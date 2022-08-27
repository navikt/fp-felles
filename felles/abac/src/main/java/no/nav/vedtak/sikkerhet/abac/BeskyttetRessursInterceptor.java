package no.nav.vedtak.sikkerhet.abac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jws.WebService;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;
import no.nav.vedtak.sikkerhet.abac.internal.ActionUthenter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

@BeskyttetRessurs(action = BeskyttetRessursActionAttributt.DUMMY, resource = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 11)
@Dependent
public class BeskyttetRessursInterceptor {

    private final Pep pep;
    private final AbacAuditlogger abacAuditlogger;
    private static final Environment ENV = Environment.current();
    private final TokenProvider tokenProvider;

    @Inject
    public BeskyttetRessursInterceptor(Pep pep, AbacAuditlogger abacAuditlogger, TokenProvider provider) {
        this.pep = pep;
        this.abacAuditlogger = abacAuditlogger;
        this.tokenProvider = provider;
    }

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {
        var attributter = hentAttributter(invocationContext);
        var dataAttributter = finnAbacDataAttributter(invocationContext);
        attributter.leggTil(dataAttributter);
        var beskyttetRessursAttributter = hentBeskyttetRessursAttributter(invocationContext, dataAttributter);
        var beslutning = pep.nyttAbacGrensesnitt() ? pep.vurderTilgang(beskyttetRessursAttributter) : pep.vurderTilgang(attributter);
        if (beslutning.fikkTilgang()) {
            return proceed(invocationContext, attributter, beslutning);
        }
        return ikkeTilgang(attributter, beslutning);
    }

    private Object proceed(InvocationContext invocationContext, AbacAttributtSamling attributter, Tilgangsbeslutning beslutning) throws Exception {
        Method method = invocationContext.getMethod();
        boolean sporingslogges = method.getAnnotation(BeskyttetRessurs.class).sporingslogg();
        if (sporingslogges) {
            Object resultat = invocationContext.proceed();
            abacAuditlogger.loggTilgang(tokenProvider.getUid(), beslutning, attributter);
            return resultat;
        }
        return invocationContext.proceed();
    }

    private Object ikkeTilgang(AbacAttributtSamling attributter, Tilgangsbeslutning beslutning) {
        abacAuditlogger.loggDeny(tokenProvider.getUid(), beslutning, attributter);

        switch (beslutning.beslutningKode()) {
            case AVSLÅTT_KODE_6 -> throw new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
            case AVSLÅTT_KODE_7 -> throw new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
            case AVSLÅTT_EGEN_ANSATT -> throw new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
            default -> throw new PepNektetTilgangException("F-608625", "Ikke tilgang");
        }
    }

    private AbacAttributtSamling hentAttributter(InvocationContext invocationContext) {
        Class<?> clazz = getOpprinneligKlasse(invocationContext);
        var method = invocationContext.getMethod();
        var serviceType = clazz.getAnnotation(WebService.class) != null ? ServiceType.WEBSERVICE : ServiceType.REST;
        var attributter = ServiceType.WEBSERVICE.equals(serviceType)
                ? AbacAttributtSamling.medSamlToken(tokenProvider.samlToken())
                : AbacAttributtSamling.medJwtToken(tokenProvider.userToken());
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);

        attributter.setActionType(mapToBeskyttetRessursActionAttributt(beskyttetRessurs));

        attributter.setResource(finnResource(beskyttetRessurs));

        attributter.setAction(utledAction(clazz, method, serviceType));
        return attributter;
    }

    private BeskyttetRessursAttributter hentBeskyttetRessursAttributter(InvocationContext invocationContext,
                                                                        AbacDataAttributter dataAttributter) {
        Class<?> clazz = getOpprinneligKlasse(invocationContext);
        var method = invocationContext.getMethod();
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);
        // Todo fjerne når relevante endepunkt er annotert - da henter vi fra beskyttetressurs . serviceType
        var serviceType = clazz.getAnnotation(WebService.class) != null ? ServiceType.WEBSERVICE : ServiceType.REST;

        var token = ServiceType.WEBSERVICE.equals(serviceType)
            ? Token.withSamlToken(tokenProvider.samlToken())
            : Token.withOidcToken(tokenProvider.userToken());

        return BeskyttetRessursAttributter.builder()
            .medUserId(tokenProvider.getUid())
            .medToken(token)
            .medServiceType(serviceType)
            .medActionType(mapToActionType(beskyttetRessurs))
            .medResourceType(finnResource(beskyttetRessurs))
            .medPepId(pep.pepId())
            .medServicePath(utledAction(clazz, method, serviceType))
            .medDataAttributter(dataAttributter)
            .build();

    }

    static AbacDataAttributter finnAbacDataAttributter(InvocationContext invocationContext) {
        var method = invocationContext.getMethod();
        var dataAttributter = AbacDataAttributter.opprett();
        var parameterDecl = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Object parameterValue = invocationContext.getParameters()[i];
            var tilpassetAnnotering = parameterDecl[i].getAnnotation(TilpassetAbacAttributt.class);
            leggTilAttributterFraParameter(dataAttributter, parameterValue, tilpassetAnnotering);
        }
        return dataAttributter;
    }

    @SuppressWarnings("rawtypes")
    static void leggTilAttributterFraParameter(AbacDataAttributter attributter, Object parameterValue, TilpassetAbacAttributt tilpassetAnnotering) {
        if (tilpassetAnnotering != null) {
            leggTil(attributter, tilpassetAnnotering, parameterValue);
        } else {
            if (parameterValue instanceof AbacDto abacDto) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                attributter.leggTil(abacDto.abacAttributter());
            } else if (parameterValue instanceof Collection collection) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                leggTilAbacDtoSamling(attributter, collection);
            }
        }
    }

    private static void leggTilAbacDtoSamling(AbacDataAttributter attributter, Collection<?> parameterValue) {
        for (Object value : parameterValue) {
            if (value instanceof AbacDto abacDto) {
                attributter.leggTil(abacDto.abacAttributter());
            } else {
                throw new TekniskException("F-261962",
                        String.format("Ugyldig input forventet at samling inneholdt bare AbacDto-er, men fant %s",
                                value != null ? value.getClass().getName() : "null"));
            }
        }
    }

    private static void leggTil(AbacDataAttributter attributter, TilpassetAbacAttributt tilpassetAnnotering, Object verdi) {
        try {
            var dataAttributter = tilpassetAnnotering.supplierClass().getDeclaredConstructor().newInstance().apply(verdi);
            attributter.leggTil(dataAttributter);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class<?> getOpprinneligKlasse(InvocationContext invocationContext) {
        Object target = invocationContext.getTarget();
        if (target instanceof TargetInstanceProxy) {
            return ((TargetInstanceProxy) target).weld_getTargetClass();
        }
        return target.getClass();
    }

    private static String utledAction(Class<?> clazz, Method method, ServiceType serviceType) {
        return ActionUthenter.action(clazz, method, serviceType);
    }

    private static ActionType mapToActionType(BeskyttetRessurs beskyttetRessurs) {
        if (!ActionType.DUMMY.equals(beskyttetRessurs.actionType())) {
            return beskyttetRessurs.actionType();
        }
        return switch (beskyttetRessurs.action()) {
            case READ -> ActionType.READ;
            case CREATE -> ActionType.CREATE;
            case DELETE -> ActionType.DELETE;
            case UPDATE -> ActionType.UPDATE;
            case DUMMY -> ActionType.DUMMY;
        };
    }

    private static BeskyttetRessursActionAttributt mapToBeskyttetRessursActionAttributt(BeskyttetRessurs beskyttetRessurs) {
        if (!ActionType.DUMMY.equals(beskyttetRessurs.actionType())) {
            return switch (beskyttetRessurs.actionType()) {
                case READ -> BeskyttetRessursActionAttributt.READ;
                case CREATE -> BeskyttetRessursActionAttributt.CREATE;
                case DELETE -> BeskyttetRessursActionAttributt.DELETE;
                case UPDATE -> BeskyttetRessursActionAttributt.UPDATE;
                default -> BeskyttetRessursActionAttributt.DUMMY;
            };
        }
        return beskyttetRessurs.action();
    }

    private static String finnResource(BeskyttetRessurs beskyttetRessurs) {
        if (!beskyttetRessurs.property().isEmpty() && ENV.getProperty(beskyttetRessurs.property()) != null) {
            return ENV.getProperty(beskyttetRessurs.property());
        } else if (!ResourceType.DUMMY.equals(beskyttetRessurs.resourceType())) {
            return beskyttetRessurs.resourceType().getResourceTypeAttribute();
        }else if (!beskyttetRessurs.resource().isEmpty()) {
            return beskyttetRessurs.resource();
        }
        return null;
    }

}

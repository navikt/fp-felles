package no.nav.vedtak.sikkerhet.abac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;
import no.nav.vedtak.sikkerhet.abac.internal.ActionUthenter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

@BeskyttetRessurs(actionType = ActionType.DUMMY, resource = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 11)
@Dependent
public class BeskyttetRessursInterceptor {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(BeskyttetRessursInterceptor.class);

    private final Pep pep;
    private final AbacAuditlogger abacAuditlogger;
    private final TokenProvider tokenProvider;

    @Inject
    public BeskyttetRessursInterceptor(Pep pep, AbacAuditlogger abacAuditlogger, TokenProvider provider) {
        this.pep = pep;
        this.abacAuditlogger = abacAuditlogger;
        this.tokenProvider = provider;
    }

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {
        var dataAttributter = finnAbacDataAttributter(invocationContext);
        var beskyttetRessursAttributter = hentBeskyttetRessursAttributter(invocationContext, dataAttributter);
        var beslutning = pep.vurderTilgang(beskyttetRessursAttributter);
        if (beslutning.fikkTilgang()) {
            return proceed(invocationContext, beslutning);
        }
        return ikkeTilgang(beslutning);
    }

    private Object proceed(InvocationContext invocationContext, Tilgangsbeslutning beslutning) throws Exception {
        Method method = invocationContext.getMethod();
        boolean sporingslogges = method.getAnnotation(BeskyttetRessurs.class).sporingslogg();
        if (!erSystembrukerKall(beslutning.beskyttetRessursAttributter()) && sporingslogges) {
            Object resultat = invocationContext.proceed();
            abacAuditlogger.loggTilgang(tokenProvider.getUid(), beslutning);
            return resultat;
        }
        return invocationContext.proceed();
    }

    private Object ikkeTilgang(Tilgangsbeslutning beslutning) {
        if (!erSystembrukerKall(beslutning.beskyttetRessursAttributter())) {
            abacAuditlogger.loggDeny(tokenProvider.getUid(), beslutning);
        } else {
            LOG.info("ABAC AVSLAG SYSTEMBRUKER {}", beslutning.beskyttetRessursAttributter().getUserId());
        }

        switch (beslutning.beslutningKode()) {
            case AVSLÅTT_KODE_6 -> throw new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
            case AVSLÅTT_KODE_7 -> throw new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
            case AVSLÅTT_EGEN_ANSATT -> throw new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
            default -> throw new PepNektetTilgangException("F-608625", "Ikke tilgang");
        }
    }

    private boolean erSystembrukerKall(BeskyttetRessursAttributter beskyttetRessursAttributter) {
        return Optional.ofNullable(beskyttetRessursAttributter)
            .map(BeskyttetRessursAttributter::getToken)
            .map(Token::getIdentType)
            .orElse(IdentType.InternBruker)
            .erSystem();
    }

    private BeskyttetRessursAttributter hentBeskyttetRessursAttributter(InvocationContext invocationContext, AbacDataAttributter dataAttributter) {
        Class<?> clazz = getOpprinneligKlasse(invocationContext);
        var method = invocationContext.getMethod();
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);
        var serviceType = beskyttetRessurs.serviceType();

        var token = ServiceType.WEBSERVICE.equals(serviceType) ? Token.withSamlToken(tokenProvider.samlToken()) : Token.withOidcToken(
            tokenProvider.openIdToken(), tokenProvider.getUid(), tokenProvider.getIdentType());

        return BeskyttetRessursAttributter.builder()
            .medUserId(tokenProvider.getUid())
            .medToken(token)
            .medServiceType(serviceType)
            .medActionType(beskyttetRessurs.actionType())
            .medAvailabilityType(beskyttetRessurs.availabilityType())
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
            if (parameterValue instanceof AbacDto abacDto) {
                attributter.leggTil(abacDto.abacAttributter());
            } else if (parameterValue instanceof Collection collection) {
                leggTilAbacDtoSamling(attributter, collection);
            }
        }
    }

    private static void leggTilAbacDtoSamling(AbacDataAttributter attributter, Collection<?> parameterValue) {
        for (Object value : parameterValue) {
            if (value instanceof AbacDto abacDto) {
                attributter.leggTil(abacDto.abacAttributter());
            } else {
                throw new TekniskException("F-261962", String.format("Ugyldig input forventet at samling inneholdt bare AbacDto-er, men fant %s",
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
        if (target instanceof TargetInstanceProxy tip) {
            return tip.weld_getTargetClass();
        }
        return target.getClass();
    }

    private static String utledAction(Class<?> clazz, Method method, ServiceType serviceType) {
        return ActionUthenter.action(clazz, method, serviceType);
    }

    private static String finnResource(BeskyttetRessurs beskyttetRessurs) {
        if (!beskyttetRessurs.property().isEmpty() && ENV.getProperty(beskyttetRessurs.property()) != null) {
            return ENV.getProperty(beskyttetRessurs.property());
        } else if (!ResourceType.DUMMY.equals(beskyttetRessurs.resourceType())) {
            return beskyttetRessurs.resourceType().getResourceTypeAttribute();
        } else if (!beskyttetRessurs.resource().isEmpty()) {
            return beskyttetRessurs.resource();
        }
        return null;
    }

}

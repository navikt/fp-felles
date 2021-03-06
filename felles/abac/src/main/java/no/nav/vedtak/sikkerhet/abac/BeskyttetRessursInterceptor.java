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
        var beslutning = pep.vurderTilgang(attributter);
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
            abacAuditlogger.loggTilgang(tokenProvider.getUid(), beslutning.getPdpRequest(), attributter);
            return resultat;
        }
        return invocationContext.proceed();
    }

    private Object ikkeTilgang(AbacAttributtSamling attributter, Tilgangsbeslutning beslutning) {
        abacAuditlogger.loggDeny(tokenProvider.getUid(), beslutning.getPdpRequest(), attributter);

        switch (beslutning.getBeslutningKode()) {
            case AVSLÅTT_KODE_6:
                throw new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
            case AVSLÅTT_KODE_7:
                throw new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
            case AVSLÅTT_EGEN_ANSATT:
                throw new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
            default:
                throw new PepNektetTilgangException("F-608625", "Ikke tilgang");
        }
    }

    private AbacAttributtSamling hentAttributter(InvocationContext invocationContext) {
        Class<?> clazz = getOpprinneligKlasse(invocationContext);
        var method = invocationContext.getMethod();
        var attributter = clazz.getAnnotation(WebService.class) != null
                ? AbacAttributtSamling.medSamlToken(tokenProvider.samlToken())
                : AbacAttributtSamling.medJwtToken(tokenProvider.userToken());
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);
        attributter.setActionType(beskyttetRessurs.action());

        if (!beskyttetRessurs.property().isEmpty()) {
            var resource = ENV.getProperty(beskyttetRessurs.property());
            attributter.setResource(resource);
        } else if (!beskyttetRessurs.resource().isEmpty()) {
            attributter.setResource(beskyttetRessurs.resource());
        }

        attributter.setAction(utledAction(clazz, method));
        var parameterDecl = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Object parameterValue = invocationContext.getParameters()[i];
            var tilpassetAnnotering = parameterDecl[i].getAnnotation(TilpassetAbacAttributt.class);
            leggTilAttributterFraParameter(attributter, parameterValue, tilpassetAnnotering);
        }
        return attributter;
    }

    @SuppressWarnings("rawtypes")
    static void leggTilAttributterFraParameter(AbacAttributtSamling attributter, Object parameterValue, TilpassetAbacAttributt tilpassetAnnotering) {
        if (tilpassetAnnotering != null) {
            leggTil(attributter, tilpassetAnnotering, parameterValue);
        } else {
            if (parameterValue instanceof AbacDto) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                attributter.leggTil(((AbacDto) parameterValue).abacAttributter());
            } else if (parameterValue instanceof Collection) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                leggTilAbacDtoSamling(attributter, (Collection) parameterValue);
            }
        }
    }

    private static void leggTilAbacDtoSamling(AbacAttributtSamling attributter, Collection<?> parameterValue) {
        for (Object value : parameterValue) {
            if (value instanceof AbacDto) {
                attributter.leggTil(((AbacDto) value).abacAttributter());
            } else {
                throw new TekniskException("F-261962",
                        String.format("Ugyldig input forventet at samling inneholdt bare AbacDto-er, men fant %s",
                                value != null ? value.getClass().getName() : "null"));
            }
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

    private static String utledAction(Class<?> clazz, Method method) {
        return ActionUthenter.action(clazz, method);
    }

    private static void leggTil(AbacAttributtSamling attributter, TilpassetAbacAttributt tilpassetAnnotering, Object verdi) {
        try {
            var dataAttributter = tilpassetAnnotering.supplierClass().getDeclaredConstructor().newInstance().apply(verdi);
            attributter.leggTil(dataAttributter);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

}

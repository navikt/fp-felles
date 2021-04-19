package no.nav.foreldrepenger.sikkerhet.abac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import no.nav.foreldrepenger.sikkerhet.abac.auditlog.AbacAuditlogger;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.pep.Pep;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PepNektetTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.util.env.Environment;


@BeskyttetRessurs(action = ActionType.DUMMY, path = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 11)
@Dependent
public class BeskyttetRessursInterceptor {

    private static final Environment ENV = Environment.current();

    private final Pep pep;
    private final AbacAuditlogger abacAuditlogger;

    @Inject
    public BeskyttetRessursInterceptor(Pep pep, AbacAuditlogger abacAuditlogger) {
        this.pep = pep;
        this.abacAuditlogger = abacAuditlogger;
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

    private BeskyttRessursAttributer hentAttributter(InvocationContext invocationContext) {
        var method = invocationContext.getMethod();
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);

        var attributter = new BeskyttRessursAttributer()
        .setServiceType(beskyttetRessurs.service())
        .setActionType(beskyttetRessurs.action())
        .setRequestPath(beskyttetRessurs.path());

        if (!beskyttetRessurs.property().isEmpty()) {
            var resource = ENV.getProperty(beskyttetRessurs.property());
            attributter.setResource(resource);
        } else if (!beskyttetRessurs.resource().isEmpty()) {
            attributter.setResource(beskyttetRessurs.resource());
        }

        // Legg på alle attributer fra AbacDtoer og AbacDtoSupplier
        var parameterDecl = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Object parameterValue = invocationContext.getParameters()[i];
            AbacDtoSupplier supplierAnnoterign = parameterDecl[i].getAnnotation(AbacDtoSupplier.class);
            leggTilAttributterFraParameter(attributter, parameterValue, supplierAnnoterign);
        }
        return attributter;
    }

    @SuppressWarnings("rawtypes")
    static void leggTilAttributterFraParameter(BeskyttRessursAttributer attributter, Object parameterValue, AbacDtoSupplier supplierAnnotering) {
        if (supplierAnnotering != null) {
            leggTil(attributter, supplierAnnotering, parameterValue);
        } else {
            if (parameterValue instanceof AbacDto) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                attributter.leggTil(((AbacDto) parameterValue).abacAttributter());
            } else if (parameterValue instanceof Collection) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                leggTilAbacDtoSamling(attributter, (Collection) parameterValue);
            }
        }
    }

    private static void leggTilAbacDtoSamling(BeskyttRessursAttributer attributter, Collection<?> parameterValue) {
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

    private static void leggTil(BeskyttRessursAttributer attributter, AbacDtoSupplier abacDtoSupplier, Object verdi) {
        try {
            var dataAttributter = abacDtoSupplier.supplierClass().getDeclaredConstructor().newInstance().apply(verdi);
            attributter.leggTil(dataAttributter);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    private Object proceed(InvocationContext invocationContext, BeskyttRessursAttributer attributter, Tilgangsbeslutning beslutning) throws Exception {
        Method method = invocationContext.getMethod();
        boolean auditlogges = method.getAnnotation(BeskyttetRessurs.class).sporingslogg();
        if (auditlogges) {
            abacAuditlogger.loggTilgang(beslutning.getPdpRequest(), attributter);
            return invocationContext.proceed();
        }
        return invocationContext.proceed();
    }

    private Object ikkeTilgang(BeskyttRessursAttributer attributter, Tilgangsbeslutning beslutning) {
        abacAuditlogger.loggDeny(beslutning.getPdpRequest(), attributter);

        switch (beslutning.getBeslutningKode()) {
            case AVSLÅTT_KODE_6 -> throw new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
            case AVSLÅTT_KODE_7 -> throw new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
            case AVSLÅTT_EGEN_ANSATT -> throw new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
            default -> throw new PepNektetTilgangException("F-608625", "Ikke tilgang");
        }
    }

}

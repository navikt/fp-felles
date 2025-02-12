package no.nav.vedtak.sikkerhet.abac;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.internal.ActionUthenter;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

@BeskyttetRessurs(actionType = ActionType.DUMMY, resourceType = ResourceType.DUMMY)
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 11)
@Dependent
public class BeskyttetRessursInterceptor {

    private final Pep pep;
    private final TokenProvider tokenProvider;

    @Inject
    public BeskyttetRessursInterceptor(Pep pep, TokenProvider provider) {
        this.pep = pep;
        this.tokenProvider = provider;
    }

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {
        var method = invocationContext.getMethod();
        var dataAttributter = finnAbacDataAttributter(method, invocationContext.getParameters());
        var beskyttetRessursAttributter = hentBeskyttetRessursAttributter(method, getOpprinneligKlasse(invocationContext), dataAttributter);

        var beslutning = pep.vurderTilgang(beskyttetRessursAttributter);
        if (beslutning.fikkTilgang()) {
            return invocationContext.proceed();
        } else {
            return ikkeTilgang(beslutning);
        }
    }

    private Object ikkeTilgang(AbacResultat abacResultat) {
        switch (abacResultat) {
            case AVSLÅTT_KODE_6 -> throw new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
            case AVSLÅTT_KODE_7 -> throw new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
            case AVSLÅTT_EGEN_ANSATT -> throw new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
            default -> throw new PepNektetTilgangException("F-608625", "Ikke tilgang");
        }
    }

    private BeskyttetRessursAttributter hentBeskyttetRessursAttributter(Method method, Class<?> mClass, AbacDataAttributter dataAttributter) {
        var beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);

        return BeskyttetRessursAttributter.builder()
            .medBrukerId(tokenProvider.getUid())
            .medBrukerOid(tokenProvider.getOid())
            .medIdentType(tokenProvider.getIdentType())
            .medAnsattGrupper(tokenProvider.getAnsattGrupper())
            .medActionType(beskyttetRessurs.actionType())
            .medAvailabilityType(beskyttetRessurs.availabilityType())
            .medResourceType(finnResource(beskyttetRessurs))
            .medSporingslogg(beskyttetRessurs.sporingslogg())
            .medServicePath(utledAction(mClass, method))
            .medDataAttributter(dataAttributter)
            .build();

    }

    static AbacDataAttributter finnAbacDataAttributter(Method method, Object[] parameters) {
        var dataAttributter = AbacDataAttributter.opprett();
        var parameterDecl = method.getParameters();
        var parameterCount = method.getParameterCount();
        for (int i = 0; i < parameterCount; i++) {
            Object parameterValue = parameters[i];
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

    private static String utledAction(Class<?> clazz, Method method) {
        return ActionUthenter.action(clazz, method);
    }

    private static ResourceType finnResource(BeskyttetRessurs beskyttetRessurs) {
        if (!ResourceType.DUMMY.equals(beskyttetRessurs.resourceType())) {
            return beskyttetRessurs.resourceType();
        }
        return null;
    }

}

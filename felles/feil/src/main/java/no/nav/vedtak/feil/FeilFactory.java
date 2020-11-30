package no.nav.vedtak.feil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public abstract class FeilFactory {
    private FeilFactory() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

    @SuppressWarnings("unchecked")
    public static <T extends DeklarerteFeil> T create(Class<T> theInterface) {
        Object proxy = Proxy.newProxyInstance(theInterface.getClassLoader(), new Class[] { theInterface }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                Throwable cause = harMedCause(method) ? (Throwable) args[args.length - 1] : null;
                if (method.getAnnotation(TekniskFeil.class) != null) {
                    return lagTekniskFeil(method, args, cause);
                }
                if (method.getAnnotation(IntegrasjonFeil.class) != null) {
                    return lagIntegrasjonFeil(method, args, cause);
                }
                if (method.getAnnotation(ManglerTilgangFeil.class) != null) {
                    return lagIkkeTilgangFeil(method, args, cause);
                }
                if (method.getAnnotation(no.nav.vedtak.feil.deklarasjon.FunksjonellFeil.class) != null) {
                    return lagFunksjonellFeil(method, args, cause);
                }
                throw new IllegalArgumentException("Mangler annotering. Må ha en av: " +
                        "@" + TekniskFeil.class.getSimpleName() +
                        ", @" + IntegrasjonFeil.class.getSimpleName() +
                        ", @" + ManglerTilgangFeil.class.getSimpleName() +
                        "eller @" + FunksjonellFeil.class.getSimpleName() +
                        "på " + method);
            }
        });
        return (T) proxy;
    }

    private static boolean harMedCause(Method method) {
        return method.getParameterCount() > 0
                && Throwable.class.isAssignableFrom(method.getParameterTypes()[method.getParameterCount() - 1]);
    }

    private static Object lagTekniskFeil(Method method, Object[] metodeargumenter, Throwable cause) {
        TekniskFeil a = method.getAnnotation(TekniskFeil.class);
        String feilmelding = String.format(a.feilmelding(), metodeargumenter);
        return new Feil(a.feilkode(), feilmelding, a.logLevel(), a.exceptionClass(), cause);
    }

    private static Object lagIntegrasjonFeil(Method method, Object[] metodeargumenter, Throwable cause) {
        IntegrasjonFeil annotering = method.getAnnotation(IntegrasjonFeil.class);
        String feilmelding = String.format(annotering.feilmelding(), metodeargumenter);
        return new Feil(annotering.feilkode(), feilmelding, annotering.logLevel(), annotering.exceptionClass(), cause);
    }

    private static Object lagIkkeTilgangFeil(Method method, Object[] metodeargumenter, Throwable cause) {
        ManglerTilgangFeil annotering = method.getAnnotation(ManglerTilgangFeil.class);
        String feilmelding = String.format(annotering.feilmelding(), metodeargumenter);
        return new Feil(annotering.feilkode(), feilmelding, annotering.logLevel(), annotering.exceptionClass(), cause);
    }

    private static Object lagFunksjonellFeil(Method method, Object[] metodeargumenter, Throwable cause) {
        no.nav.vedtak.feil.deklarasjon.FunksjonellFeil annotering = method.getAnnotation(no.nav.vedtak.feil.deklarasjon.FunksjonellFeil.class);
        String feilmelding = String.format(annotering.feilmelding(), metodeargumenter);
        return new FunksjonellFeil(
                annotering.feilkode(), feilmelding, annotering.løsningsforslag(), annotering.logLevel(), annotering.exceptionClass(), cause);
    }

}

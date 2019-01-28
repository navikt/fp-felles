package no.nav.vedtak.feil.doc;

import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.lang.reflect.Method;

public class FeilUtil {
    public static String feilkode(Method method) {
        TekniskFeil t = method.getAnnotation(TekniskFeil.class);
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        IntegrasjonFeil i = method.getAnnotation(IntegrasjonFeil.class);
        ManglerTilgangFeil m = method.getAnnotation(ManglerTilgangFeil.class);
        if (t != null) {
            return t.feilkode();
        }
        if (f != null) {
            return f.feilkode();
        }
        if (i != null) {
            return i.feilkode();
        }
        if (m != null) {
            return m.feilkode();
        }
        return null;
    }

    public static LogLevel logLevel(Method method) {
        TekniskFeil t = method.getAnnotation(TekniskFeil.class);
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        IntegrasjonFeil i = method.getAnnotation(IntegrasjonFeil.class);
        ManglerTilgangFeil m = method.getAnnotation(ManglerTilgangFeil.class);
        if (t != null) {
            return t.logLevel();
        }
        if (f != null) {
            return f.logLevel();
        }
        if (i != null) {
            return i.logLevel();
        }
        if (m != null) {
            return m.logLevel();
        }
        return null;
    }

    public static String type(Method method) {
        TekniskFeil t = method.getAnnotation(TekniskFeil.class);
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        IntegrasjonFeil i = method.getAnnotation(IntegrasjonFeil.class);
        ManglerTilgangFeil m = method.getAnnotation(ManglerTilgangFeil.class);
        if (t != null) {
            return TekniskFeil.class.getSimpleName();
        }
        if (f != null) {
            return FunksjonellFeil.class.getSimpleName();
        }
        if (i != null) {
            return IntegrasjonFeil.class.getSimpleName();
        }
        if (m != null) {
            return ManglerTilgangFeil.class.getSimpleName();
        }
        return null;
    }

    public static String feilmelding(Method method) {
        TekniskFeil t = method.getAnnotation(TekniskFeil.class);
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        IntegrasjonFeil i = method.getAnnotation(IntegrasjonFeil.class);
        ManglerTilgangFeil m = method.getAnnotation(ManglerTilgangFeil.class);
        if (t != null) {
            return t.feilmelding();
        }
        if (f != null) {
            return f.feilmelding();
        }
        if (i != null) {
            return i.feilmelding();
        }
        if (m != null) {
            return m.feilmelding();
        }
        return null;
    }

    public static String løsningsforslag(Method method) {
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        if (f != null) {
            return f.løsningsforslag();
        }
        return null;
    }

    public static boolean harMedCause(Method method) {
        return method.getParameterCount() > 0
            && Exception.class.isAssignableFrom(method.getParameterTypes()[method.getParameterCount() - 1]);
    }

    public static int tellParametreUtenomCause(Method method) {
        return harMedCause(method)
            ? method.getParameterCount() - 1
            : method.getParameterCount();
    }

    @SuppressWarnings("unchecked")
    public static Class<Exception> deklarertCause(Method method) {
        return (Class<Exception>) method.getParameters()[method.getParameterCount() - 1].getType();
    }
}

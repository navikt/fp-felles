package no.nav.vedtak.feil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

class FeilUtil {

    @SuppressWarnings("unchecked")
    static List<Class<? extends DeklarerteFeil>> finnAlleDeklarerteFeil() {
        List<Class<? extends DeklarerteFeil>> deklarerteFeil = new ArrayList<>();
        try (var scanResult = new ClassGraph().enableClassInfo().ignoreClassVisibility().scan();) {
            scanResult.getSubclasses(DeklarerteFeil.class.getName())
                    .forEach(c -> deklarerteFeil.add((Class<? extends DeklarerteFeil>) c.loadClass()));
        }

        // ikke noen grunn til å ta med feil som er deklarert i test-scope i denne
        // modulen
        deklarerteFeil.remove(FeilFactoryTest.TestFeil.class);

        return deklarerteFeil;
    }

    static String feilkode(Method method) {
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

    static LogLevel logLevel(Method method) {
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

    static String type(Method method) {
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

    static String feilmelding(Method method) {
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

    static String løsningsforslag(Method method) {
        FunksjonellFeil f = method.getAnnotation(FunksjonellFeil.class);
        if (f != null) {
            return f.løsningsforslag();
        }
        return null;
    }

    static boolean harMedCause(Method method) {
        return method.getParameterCount() > 0
                && Throwable.class.isAssignableFrom(method.getParameterTypes()[method.getParameterCount() - 1]);
    }

    static int tellParametreUtenomCause(Method method) {
        return harMedCause(method) ? method.getParameterCount() - 1 : method.getParameterCount();
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> Class<T> deklarertCause(Method method) {
        return (Class<T>) method.getParameters()[method.getParameterCount() - 1].getType();
    }
}

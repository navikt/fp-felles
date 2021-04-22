package no.nav.vedtak.util.env;

import static java.lang.System.getenv;

import java.util.Optional;
@Deprecated(since = "3.2.x", forRemoval = true)
/* Bruk klasser fra no.nav.foreldrepenger:konfig:1.1 istedenfor. */
public class Namespace {
    public static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE";

    private static final String DEFAULT_NAMESPACE = "default";

    private final String namespace;

    private Namespace(String name) {
        this.namespace = name;
    }

    public static Namespace of(String name) {
        return new Namespace(name);
    }

    public String getNamespace() {
        return namespace;
    }

    public static Namespace current() {
        return Namespace.of(Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
                .orElse(DEFAULT_NAMESPACE));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[namespace=" + namespace + "]";
    }

}

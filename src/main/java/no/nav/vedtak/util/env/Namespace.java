package no.nav.vedtak.util.env;

import static java.lang.System.getenv;

import java.util.Optional;

public class Namespace {
    public static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE";

    private static final String DEFAULT_NAMESPACE = "default";

    private final String name;

    private Namespace(String name) {
        this.name = name;
    }

    public static Namespace of(String name) {
        return new Namespace(name);
    }

    public String getName() {
        return name;
    }

    public static Namespace current() {
        return Namespace.of(Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
                .orElse(DEFAULT_NAMESPACE));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[namespace=" + name + "]";
    }

}

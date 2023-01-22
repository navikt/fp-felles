package no.nav.foreldrepenger.konfig;

import static java.lang.System.getenv;

import java.util.Optional;

public class Namespace {

    private static final String DEFAULT_NAMESPACE = "teamforeldrepenger";

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
        return Namespace.of(Optional.ofNullable(getenv(NaisProperty.NAMESPACE.propertyName()))
                .orElse(DEFAULT_NAMESPACE));
    }

    public static Namespace foreldrepenger() {
        return Namespace.of(DEFAULT_NAMESPACE);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[namespace=" + name + "]";
    }

}

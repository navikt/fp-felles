package no.nav.foreldrepenger.konfig;

import static java.lang.System.getenv;

public class Application {

    private final String name;

    private Application(String name) {
        this.name = name;
    }

    public static Application of(String name) {
        return new Application(name);
    }

    public String getName() {
        return name;
    }

    public static Application current() {
        return Application.of(getenv(NaisProperty.APPLICATION.propertyName()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[application=" + name + "]";
    }

}

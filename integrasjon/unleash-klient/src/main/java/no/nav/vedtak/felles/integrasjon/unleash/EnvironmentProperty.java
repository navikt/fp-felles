package no.nav.vedtak.felles.integrasjon.unleash;

import static no.nav.vedtak.util.env.Namespace.NAIS_NAMESPACE_NAME;

import java.util.Optional;

import no.nav.vedtak.util.env.Environment;

public class EnvironmentProperty {

    private static final Environment ENV = Environment.current();
    static final String APP_NAME = "NAIS_APP_NAME";

    private EnvironmentProperty() {

    }

    public static Optional<String> getEnvironmentName() {
        return Optional.of(ENV.getProperty(NAIS_NAMESPACE_NAME));
    }

    static Optional<String> getAppName() {
        return Optional.ofNullable(ENV.getProperty(APP_NAME))
                .or(() -> Optional.ofNullable(ENV.getProperty("application.name")));
    }
}

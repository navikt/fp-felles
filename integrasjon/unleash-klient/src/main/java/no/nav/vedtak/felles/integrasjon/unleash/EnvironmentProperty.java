package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.Optional;

import no.nav.vedtak.konfig.PropertyUtil;

public class EnvironmentProperty {

    public static final String NAIS_NAMESPACE = "NAIS_NAMESPACE";
    public static final String APP_NAME = "NAIS_APP_NAME";
    public static final String FASIT_ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";

    private EnvironmentProperty() {
    }

    public static Optional<String> getEnvironmentName() {
        String environmentName = PropertyUtil.getProperty(FASIT_ENVIRONMENT_NAME);
        if (environmentName != null) {
            return Optional.of(environmentName);
        }
        environmentName = PropertyUtil.getProperty(NAIS_NAMESPACE);
        if (environmentName != null) {
            return Optional.of(environmentName);
        }
        return Optional.ofNullable(PropertyUtil.getProperty("environment.name"));
    }

    static String getApplicationName() {
        var appName = getAppName();
        if (!appName.isPresent()) {
            throw EnvironmentFeil.FACTORY.manglerApplicationNameProperty().toException();
        }
        return appName.get();
    }

    static Optional<String> getAppName() {
        String appName = PropertyUtil.getProperty(APP_NAME);
        if (appName == null) {
            appName = PropertyUtil.getProperty("application.name");
        }
        return Optional.ofNullable(appName);
    }
}

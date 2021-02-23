package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByEnvironmentStrategy;
import no.nav.vedtak.util.env.Environment;

public class ToggleConfig {

    public static final String PROD = "default";
    public static final String UNLEASH_API_OVERRIDE_KEY = "UNLEASH_API_OVERRIDE";
    private static final String DEFAULT_UNLEASH_API = "http://unleash.default.svc.nais.local/api/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleConfig.class);
    private final Optional<String> appName;
    private final String instanceName;

    ToggleConfig() {
        this.appName = EnvironmentProperty.getAppName();
        if (this.appName.isPresent()) {
            this.instanceName = EnvironmentProperty.getEnvironmentName().orElse(Environment.current().getProperty(UNLEASH_API_OVERRIDE_KEY, PROD));
        } else {
            this.instanceName = "test";
        }
    }

    private static String overrideOrDefault(String key, String defaultValue) {
        return Environment.current().getProperty(key, defaultValue);
    }

    public Unleash unleash() {
        if (appName.isPresent()) {
            UnleashConfig config = UnleashConfig.builder()
                    .appName(appName.get())
                    .instanceId(instanceName)
                    .unleashAPI(overrideOrDefault(UNLEASH_API_OVERRIDE_KEY, DEFAULT_UNLEASH_API))
                    .build();

            LOGGER.info("Oppretter unleash strategier med appName={} and instanceName={}", this.appName.get(), this.instanceName);
            return new DefaultUnleash(config, addStrategies());
        }
        LOGGER.warn("Benytter FakeUnleash, NAIS_APP_NAME ikke definert");
        return new FakeUnleash();
    }

    private Strategy[] addStrategies() {
        return new Strategy[] { new ByEnvironmentStrategy(), new ByAnsvarligSaksbehandlerStrategy() };
    }
}

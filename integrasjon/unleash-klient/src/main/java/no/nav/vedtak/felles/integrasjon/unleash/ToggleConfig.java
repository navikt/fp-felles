package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

public class ToggleConfig {

    public static final String PROD = "default";
    public static final String UNLEASH_API_OVERRIDE_KEY = "UNLEASH_API_OVERRIDE";
    private static final String DEFAULT_UNLEASH_API = "https://unleash.nais.adeo.no/api/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleConfig.class);
    private Optional<String> appName;
    private String instanceName;

    /**
     * Lag en ny ToggleConfig
     */
    ToggleConfig() {
        this.appName = EnvironmentProperty.getAppName();
        if (this.appName.isPresent()) {
            this.instanceName = EnvironmentProperty.getEnvironmentName().orElse(overrideOrDefault(UNLEASH_API_OVERRIDE_KEY, PROD));
        } else {
            this.instanceName = "test";
        }

    }

    private static String overrideOrDefault(String key, String defaultValue) {
        Objects.requireNonNull(key, "Environment key cannot be null");
        final var overridedUnleashUrl = System.getenv(key);
        if (overridedUnleashUrl != null && !overridedUnleashUrl.isBlank()) {
            return overridedUnleashUrl;
        }
        return defaultValue;
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
        } else {
            LOGGER.warn("Benytter FakeUnleash, NAIS_APP_NAME ikke definert");
            return new FakeUnleash();
        }
    }

    private Strategy[] addStrategies() {
        List<Strategy> list = new ArrayList<>(Arrays.asList(
            new ByEnvironmentStrategy(), new ByAnsvarligSaksbehandlerStrategy()));
        return list.toArray(new Strategy[0]);
    }

}

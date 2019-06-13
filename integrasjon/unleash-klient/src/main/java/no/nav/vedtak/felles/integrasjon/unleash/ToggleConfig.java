package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private static final String UNLEASH_API = "https://unleash.nais.adeo.no/api/";
    public static final String PROD = "default";
    private Optional<String> appName;
    private String instanceName;
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleConfig.class);

    /**
     * Lag en ny ToggleConfig
     *
     */
    ToggleConfig() {
        this.appName = EnvironmentProperty.getAppName();
        if (this.appName.isPresent()) {
            this.instanceName = EnvironmentProperty.getEnvironmentName().orElse(PROD);
        } else {
            this.instanceName = "test";
        }

    }

    public Unleash unleash() {
        if (appName.isPresent()) {
            UnleashConfig config = UnleashConfig.builder()
                .appName(appName.get())
                .instanceId(instanceName)
                .unleashAPI(UNLEASH_API)
                .build();

            LOGGER.info("Oppretter unleash strategier med appName={} and instanceName={}", this.appName.get(), this.instanceName);
            return new DefaultUnleash(config, addStrategies());
        } else {
            LOGGER.info("Benytter FakeUnleash, NAIS_APP_NAME ikke definert");
            return new FakeUnleash();
        }
    }

    private Strategy[] addStrategies() {
        List<Strategy> list = new ArrayList<>(Arrays.asList(
            new ByEnvironmentStrategy(), new ByAnsvarligSaksbehandlerStrategy()));
        return list.toArray(new Strategy[0]);
    }

}

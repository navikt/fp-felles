package no.nav.vedtak.felles.integrasjon.unleash;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByEnvironmentStrategy;

public class ToggleConfig {

    private static final String UNLEASH_API = "https://unleash.nais.adeo.no/api/";
    private String appName;
    private String instanceName;
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleConfig.class);

    /**
     * Lag en ny ToggleConfig
     *
     */
    ToggleConfig() {
        this.appName = EnvironmentProperty.getApplicationName();
        this.instanceName = EnvironmentProperty.getEnvironmentName().orElse("devimg");
    }

    public Unleash unleash() {
        UnleashConfig config = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceName)
                .unleashAPI(UNLEASH_API)
                .build();

        LOGGER.info("Oppretter unleash strategier med appName={} and instanceName={}", this.appName, this.instanceName);
        return new DefaultUnleash(config, addStrategies());
    }

    private Strategy[] addStrategies() {
        List<Strategy> list = new ArrayList<>(Arrays.asList(
                new ByEnvironmentStrategy(), new ByAnsvarligSaksbehandlerStrategy()
        ));
        return list.toArray(new Strategy[0]);
    }

}

package no.nav.vedtak.felles.integrasjon.unleash;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByAnsvarligSaksbehandlerStrategy;
import no.nav.vedtak.felles.integrasjon.unleash.strategier.ByEnvironmentStrategy;
import no.nav.vedtak.util.env.Environment;

public class ToggleConfig {
    private static final Environment ENV = Environment.current();

    public static final String PROD = "default";
    public static final String UNLEASH_API_OVERRIDE_KEY = "UNLEASH_API_OVERRIDE";
    private static final String DEFAULT_UNLEASH_API = "http://unleash.default.svc.nais.local/api/";
    private static final Logger LOGGER = LoggerFactory.getLogger(ToggleConfig.class);
    private final String appName;
    private final String instanceName;

    ToggleConfig() {
        appName = ENV.appName();
        if (appName != null) {
            this.instanceName = Optional.of(ENV.namespace()).orElseGet(() -> ENV.getProperty(UNLEASH_API_OVERRIDE_KEY, PROD));
        } else {
            this.instanceName = "test";
        }
    }

    public Unleash unleash() {
        if (appName != null) {
            UnleashConfig config = UnleashConfig.builder()
                    .appName(appName)
                    .instanceId(instanceName)
                    .unleashAPI(ENV.getProperty(UNLEASH_API_OVERRIDE_KEY, DEFAULT_UNLEASH_API))
                    .build();

            LOGGER.info("Oppretter unleash strategier med appName={} and instanceName={}", appName, this.instanceName);
            return new DefaultUnleash(config, new ByEnvironmentStrategy(), new ByAnsvarligSaksbehandlerStrategy());
        }
        LOGGER.warn("Benytter FakeUnleash, appName ikke definert");
        return new FakeUnleash();
    }

}

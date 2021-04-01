package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.strategy.Strategy;

public class ByEnvironmentStrategy implements Strategy {
    public static final String ENV_KEY = "miljø";
    private static final Logger LOGGER = LoggerFactory.getLogger(ByEnvironmentStrategy.class);

    @Override
    public String getName() {
        return "byEnvironment";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        boolean enabled = NamespaceUtil.isNamespaceEnabled(parameters, ENV_KEY);
        LOGGER.debug("Strategy={} is enabled={}", getName(), enabled);
        return enabled;
    }
}

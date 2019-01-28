package no.nav.vedtak.felles.integrasjon.unleash.strategier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.unleash.EnvironmentProperty;

class EnvironmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentService.class);

    private EnvironmentService() {
    }

    static boolean isCurrentEnvironmentInMap(Map<String, String> parameters, String envKey) {
        Optional<String> currentEnvironmentOpt = EnvironmentProperty.getEnvironmentName();
        List<String> enabledEnvironments = parseEnvironments(parameters, envKey);
        LOGGER.info("Current Environment={}, Enabled Environments: {}", currentEnvironmentOpt, enabledEnvironments);

        return currentEnvironmentOpt
                .map(currentEnvironment -> enabledEnvironments.stream()
                        .anyMatch(currentEnvironment::equalsIgnoreCase))
                .orElse(false);
    }

    private static List<String> parseEnvironments(Map<String, String> parameters, String envKey) {
        Optional<String> commaSeparatedEnvironments = Optional.ofNullable(parameters)
                .map(par -> par.get(envKey));
        Stream<String> enabledEnvironments = commaSeparatedEnvironments
                .filter(s -> !s.isEmpty())
                .map(envs -> envs.split(","))
                .stream()
                .flatMap(Arrays::stream);
        return enabledEnvironments
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

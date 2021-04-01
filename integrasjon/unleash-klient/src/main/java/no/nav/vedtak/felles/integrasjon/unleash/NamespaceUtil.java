package no.nav.vedtak.felles.integrasjon.unleash;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

class NamespaceUtil {
    private static final Environment ENV = Environment.current();

    private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceUtil.class);

    private NamespaceUtil() {
    }

    static boolean isNamespaceEnabled(Map<String, String> parameters, String envKey) {
        var namespace = Optional.of(ENV.namespace());
        var enabledNamespaces = enabledNamespaces(parameters, envKey);
        LOGGER.debug("Current namespace={}, Enabled namespaces: {}", namespace, enabledNamespaces);

        return namespace
                .map(ns -> enabledNamespaces.stream()
                        .anyMatch(ns::equalsIgnoreCase))
                .orElse(false);
    }

    private static List<String> enabledNamespaces(Map<String, String> parameters, String envKey) {
        return Optional.ofNullable(parameters)
                .map(par -> par.get(envKey))
                .filter(not(String::isEmpty))
                .map(envs -> envs.split(","))
                .stream()
                .flatMap(Arrays::stream)
                .map(String::trim)
                .collect(toList());
    }
}

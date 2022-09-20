package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Methods to extract information from RestClientConfig annotations. Presedence
 * - endpointProperty and scopesProperty if specified and the property contains a value
 * - derived from FpApplication if specified. The endpoint is the contextPath, and rarely of direct use
 * - endpointDefault end scopesDefault
 */
public final class RestConfig {

    private static final Environment ENV = Environment.current();

    public static URI endpointFromAnnotation(Class<?> clazz) {
        return fromAnnotation(clazz, FpApplication::contextPathFor, RestClientConfig::endpointProperty, RestClientConfig::endpointDefault)
            .map(URI::create)
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler endpoint for " + clazz.getSimpleName()));
    }

    public static URI contextPathFromAnnotation(Class<?> clazz) {
        return fromAnnotation(clazz, FpApplication::contextPathFor, c -> "", c -> "")
            .map(URI::create)
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler application for " + clazz.getSimpleName()));
    }

    public static Optional<FpApplication> applicationFromAnnotation(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class))
            .map(RestClientConfig::application)
            .filter(FpApplication::specified);
    }

    public static TokenFlow tokenConfigFromAnnotation(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class))
            .map(RestClientConfig::tokenConfig).orElse(TokenFlow.CONTEXT);
    }

    public static String scopesFromAnnotation(Class<?> clazz) {
        var scopesFound = fromAnnotation(clazz, FpApplication::scopesFor, RestClientConfig::scopesProperty, RestClientConfig::scopesDefault);
        // Exception if target requires scopes
        if (scopesFound.isEmpty() && Set.of(TokenFlow.AZUREAD_CC, TokenFlow.CONTEXT_AZURE).contains(tokenConfigFromAnnotation(clazz))) {
            throw new IllegalArgumentException("Utviklerfeil: mangler scopes for " + clazz.getSimpleName());
        }
        return scopesFound.orElse(null);
    }

    private static Optional<String> fromAnnotation(Class<?> clazz,
                                                   Function<FpApplication, String> internal,
                                                   Function<RestClientConfig, String> selector,
                                                   Function<RestClientConfig, String> defaultValue) {
        var annotation = Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class));
        return annotation.flatMap(a -> nonEmpty(a, selector))
            .map(ENV::getProperty)
            .or(() -> annotation.filter(a -> a.application().specified()).map(RestClientConfig::application).map(internal))
            .or(() -> annotation.flatMap(a -> nonEmpty(a, defaultValue)));
    }

    private static Optional<String> nonEmpty(RestClientConfig config, Function<RestClientConfig, String> selector) {
        var value = selector.apply(config);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

}

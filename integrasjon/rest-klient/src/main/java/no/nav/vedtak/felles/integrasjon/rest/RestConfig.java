package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Object to hold information extracted from RestClientConfig annotations.
 * Presedence
 * - endpointProperty and scopesProperty if specified and the property contains a value
 * - derived from FpApplication if specified. The endpoint is the contextPath, and rarely of direct use
 * - endpointDefault end scopesDefault
 * FpContextPath will only hold a value if the annotation contains a specified application
 */
public record RestConfig(TokenFlow tokenConfig, URI endpoint, String scopes, URI fpContextPath) {

    public Optional<URI> getContextPath() {
        return Optional.ofNullable(fpContextPath);
    }

    public static RestConfig forClient(Class<?> clazz) {
        var config = Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class))
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler annotering @RestClientConfig for " + clazz.getSimpleName()));
        return forConfig(config);
    }

    public static RestConfig forConfig(RestClientConfig config) {
        var tokenConfig = tokenConfigFromAnnotation(config);
        var endpoint = endpointFromAnnotation(config);
        var fpContextPath = contextPathFromAnnotation(config).orElse(null);
        var scopes = scopesFromAnnotation(config);
        return new RestConfig(tokenConfig, endpoint, scopes, fpContextPath);
    }

    private static final Environment ENV = Environment.current();
    private static final Set<TokenFlow> REQUIRE_SCOPE = Set.of(TokenFlow.AZUREAD_CC);

    private static URI endpointFromAnnotation(RestClientConfig config) {
        return fromAnnotation(config, FpApplication::contextPathFor, RestClientConfig::endpointProperty, RestClientConfig::endpointDefault)
            .map(URI::create)
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler endpoint for " + config));
    }

    private static Optional<URI> contextPathFromAnnotation(RestClientConfig config) {
        return Optional.of(config).map(RestClientConfig::application)
            .filter(FpApplication::specified)
            .map(FpApplication::contextPathFor)
            .map(URI::create);
    }

    private static TokenFlow tokenConfigFromAnnotation(RestClientConfig config) {
        return config.tokenConfig();
    }

    private static String scopesFromAnnotation(RestClientConfig config) {
        var scopesFound = fromAnnotation(config, FpApplication::scopesFor, RestClientConfig::scopesProperty, RestClientConfig::scopesDefault);
        // Exception if target requires scopes
        if (scopesFound.isEmpty() && REQUIRE_SCOPE.contains(tokenConfigFromAnnotation(config))) {
            throw new IllegalArgumentException("Utviklerfeil: mangler scopes for " + config);
        }
        return scopesFound.orElse(null);
    }

    private static Optional<String> fromAnnotation(RestClientConfig annotation,
                                                   Function<FpApplication, String> internal,
                                                   Function<RestClientConfig, String> selector,
                                                   Function<RestClientConfig, String> defaultValue) {
        return nonEmpty(annotation, selector)
            .map(ENV::getProperty)
            .or(() -> Optional.of(annotation).map(RestClientConfig::application).filter(FpApplication::specified).map(internal))
            .or(() -> nonEmpty(annotation, defaultValue));
    }

    private static Optional<String> nonEmpty(RestClientConfig config, Function<RestClientConfig, String> selector) {
        var value = selector.apply(config);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

}

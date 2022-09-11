package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Methods to extract information from RestClientConfig annotations
 */
public final class RestConfig {

    private static final Environment ENV = Environment.current();

    public static URI endpointFromAnnotation(Class<?> clazz) {
        return fromAnnotation(clazz, FpApplication::contextPathFor, RestClientConfig::endpointProperty, RestClientConfig::endpointDefault)
            .map(URI::create)
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler endpoint for " + clazz.getSimpleName()));
    }

    public static Optional<FpApplication> applicationFromAnnotation(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class))
            .map(RestClientConfig::application)
            .filter(FpApplication::specified);
    }

    static TokenFlow tokenConfigFromAnnotation(Class<?> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class))
            .map(RestClientConfig::tokenConfig).orElse(TokenFlow.CONTEXT);
    }

    static String scopesFromAnnotation(Class<?> clazz) {
        return fromAnnotation(clazz, FpApplication::scopesFor, RestClientConfig::scopesProperty, RestClientConfig::scopesDefault)
            .orElseThrow(() -> new IllegalArgumentException("Utviklerfeil: mangler endpoint for " + clazz.getSimpleName()));
    }

    private static Optional<String> fromAnnotation(Class<?> clazz,
                                                   Function<FpApplication, String> internal,
                                                   Function<RestClientConfig, String> selector,
                                                   Function<RestClientConfig, String> defaultValue) {
        var annotation = Optional.ofNullable(clazz.getAnnotation(RestClientConfig.class));
        if (annotation.filter(a -> a.application().specified()).isPresent()) {
            return annotation.map(RestClientConfig::application).map(internal);
        }
        return annotation.flatMap(a -> nonEmpty(a, selector))
            .map(ENV::getProperty)
            .or(() -> annotation.flatMap(a -> nonEmpty(a, defaultValue)));
    }

    private static Optional<String> nonEmpty(RestClientConfig config, Function<RestClientConfig, String> selector) {
        var value = selector.apply(config);
        return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

}

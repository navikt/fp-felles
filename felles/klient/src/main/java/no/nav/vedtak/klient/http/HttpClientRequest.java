package no.nav.vedtak.klient.http;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulation of java.net.http.HttpRequest to supply specific headers and ensure a timeout is set.
 * Supports delayed header-setting and request validation + headers for authorization / basic
 * <p>
 * Usage: Create an ordinary HttpRequest.Builder with URI, Method, and headers. Then create a HttpKlientRequest
 */
public class HttpClientRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientRequest.class);

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpRequest.Builder builder;
    private Duration timeout;
    private final Map<String, Supplier<String>> headers;
    private final List<Consumer<HttpRequest>> validators;
    private boolean built;

    private HttpClientRequest() {
        this(HttpRequest.newBuilder(), null);
    }

    protected HttpClientRequest(HttpRequest.Builder builder, Map<String, Supplier<String>> headers) {
        this.builder = builder;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.validators = new ArrayList<>(List.of(HttpClientRequest::validateTimeout));
        this.built = false;
    }

    public HttpClientRequest timeout(Duration timeout) {
        validateTimeout(timeout);
        this.timeout = timeout;
        return this;
    }

    public HttpClientRequest delayedHeader(String header, Supplier<String> value) {
        headers.put(header, value);
        return this;
    }

    public HttpClientRequest validator(Consumer<HttpRequest> validator) {
        validators.add(validator);
        return this;
    }

    HttpRequest request() {
        if (built) {
            // Logg forekomster - vil unngå gjenbruk av requests
            LOG.warn("HttpRequest already built", new IllegalStateException("HttpRequest already built"));
        }
        this.built = true;
        var requestBuilder = builder.copy();
        requestBuilder.timeout(Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT));
        headers.forEach((key, value) -> requestBuilder.header(key, value.get()));
        var request = requestBuilder.build();
        validators.forEach(v -> v.accept(request));
        return request;
    }

    protected HttpRequest.Builder getBuilder() {
        return builder;
    }

    private static void validateTimeout(HttpRequest request) {
        validateTimeout(request.timeout().orElse(Duration.ZERO));
    }

    private static void validateTimeout(Duration timeout) {
        if (timeout == null || Duration.ZERO.equals(timeout) || timeout.isNegative()) {
            throw new IllegalArgumentException("Utviklerfeil: ulovlig timeout");
        }
    }

    // Test-supporting methods
    public void validateRequest(Consumer<HttpRequest> validator) {
        validator.accept(builder.copy().build());
    }

    public boolean validateDelayedHeaders(Set<String> wantedHeaders) {
        return headers.keySet().containsAll(wantedHeaders);
    }

}

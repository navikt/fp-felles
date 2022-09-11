package no.nav.vedtak.klient.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import no.nav.vedtak.log.mdc.MDCOperations;

/**
 * Encapsulation of java.net.http.HttpRequest to supply specific headers and ensure a timeout is set.
 * Supports delayed header-setting and request validation + headers for authorization / basic
 *
 * Usage: Create an ordinary HttpRequest.Builder with URI, Method, and headers. Then create a HttpKlientRequest
 */
public class HttpClientRequest {

    protected static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_FORM_ENCODED = "application/x-www-form-urlencoded";

    private static final String HEADER_NAV_CALLID = "Nav-Callid";
    private static final String HEADER_NAV_CALL_ID = "nav-call-id";

    private static final Map<String, Supplier<String>> DEFAULT_CALLID =
        Map.of(HEADER_NAV_CALLID, ensureCallId(), HEADER_NAV_CALL_ID, ensureCallId());

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final HttpRequest.Builder builder;
    private Duration timeout;
    private final Map<String, Supplier<String>> headers;
    private final List<Consumer<HttpRequest>> validators;

    private HttpClientRequest() {
        this(HttpRequest.newBuilder());
    }

    protected HttpClientRequest(HttpRequest.Builder builder) {
        this(builder, true);
    }

    private HttpClientRequest(HttpRequest.Builder builder, boolean medCallId) {
        this.builder = builder;
        this.headers = medCallId ? new HashMap<>(DEFAULT_CALLID) : new HashMap<>();
        this.validators = new ArrayList<>(List.of(HttpClientRequest::validateTimeout));
    }

    public static HttpClientRequest plain(HttpRequest.Builder builder) {
        return new HttpClientRequest(builder, false);
    }

    public static HttpClientRequest callId(HttpRequest.Builder builder) {
        return new HttpClientRequest(builder, true);
    }

    public HttpClientRequest timeout(Duration timeout) {
        validateTimeout(timeout);
        this.timeout = timeout;
        return this;
    }

    public HttpClientRequest header(String header, String value) {
        builder.header(header, value);
        return this;
    }

    public HttpClientRequest delayedHeader(String header, Supplier<String> value) {
        headers.put(header, value);
        return this;
    }

    public HttpClientRequest consumerId(Supplier<String> consumerId) {
        headers.put(HEADER_NAV_CONSUMER_ID, consumerId);
        return this;
    }

    public HttpClientRequest consumerId(String consumerId) {
        builder.header(HEADER_NAV_CONSUMER_ID, consumerId);
        return this;
    }

    public HttpClientRequest otherCallId(String header) {
        headers.put(header, ensureCallId());
        return this;
    }

    public HttpClientRequest basicAuth(String username, String password) {
        builder.header(CONTENT_TYPE, APPLICATION_FORM_ENCODED)
                .header(AUTHORIZATION, basicCredentials(username, password));
        return this;
    }

    public HttpClientRequest validator(Consumer<HttpRequest> validator) {
        validators.add(validator);
        return this;
    }

    HttpRequest request() {
        builder.timeout(Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT));
        headers.forEach((key, value) -> builder.header(key, value.get()));
        var request = builder.build();
        validators.forEach(v -> v.accept(request));
        return request;
    }

    protected static Supplier<String> ensureCallId() {
        return () -> Optional.ofNullable(MDCOperations.getCallId())
            .orElseGet(() -> {
                MDCOperations.putCallId();
                return MDCOperations.getCallId();
            });
    }

    private static String basicCredentials(String username, String password) {
        return BASIC_AUTH_HEADER_PREFIX + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }

    private static void validateTimeout(HttpRequest request) {
        validateTimeout(request.timeout().orElse(Duration.ZERO));
    }

    private static void validateTimeout(Duration timeout) {
        if (Duration.ZERO.equals(timeout) || timeout == null || timeout.isNegative()) {
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

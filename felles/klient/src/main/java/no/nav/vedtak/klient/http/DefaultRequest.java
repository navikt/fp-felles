package no.nav.vedtak.klient.http;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

import no.nav.vedtak.log.mdc.MDCOperations;

public final class DefaultRequest {

    private static final String HEADER_NAV_CALLID = "Nav-Callid";
    private static final String HEADER_NAV_CALL_ID = "nav-call-id";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    private static final String AUTHORIZATION = "Authorization";
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MEDIA_TYPE_JSON = "application/json";

    private static final Set<String> REQUIRED_HEADERS = Set.of(HEADER_NAV_CALLID, HEADER_NAV_CONSUMER_ID, AUTHORIZATION);

    private DefaultRequest() {
        // NOSONAR
    }

    public static HttpRequest.Builder builder(Supplier<String> authToken, Supplier<String> consumerId) {
        return HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(10))
            .header(HEADER_NAV_CALLID, MDCOperations.getCallId())
            .header(HEADER_NAV_CALL_ID, MDCOperations.getCallId())
            .header(HEADER_NAV_CONSUMER_ID, consumerId.get())
            .header(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + authToken.get());
    }

    public static void consumerToken(HttpRequest.Builder builder, Supplier<String> consumerToken) {
        builder.header(HEADER_NAV_CONSUMER_TOKEN, consumerToken.get());
    }

    public static void xCorrelationId(HttpRequest.Builder builder) {
        builder.header(HEADER_CORRELATION_ID, MDCOperations.getCallId());
    }

    public static void acceptJson(HttpRequest.Builder builder) {
        accept(builder, MEDIA_TYPE_JSON);
    }

    public static void accept(HttpRequest.Builder builder, String mediaType) {
        builder.header(ACCEPT, mediaType);
    }

    public static void contentTypeJson(HttpRequest.Builder builder) {
        accept(builder, MEDIA_TYPE_JSON);
    }

    public static void contentType(HttpRequest.Builder builder, String mediaType) {
        builder.header(CONTENT_TYPE, mediaType);
    }

    static void validateHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(REQUIRED_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }


}

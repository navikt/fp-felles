package no.nav.vedtak.klient.http;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

import no.nav.vedtak.log.mdc.MDCOperations;

public final class DefaultRequest {

    private static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    private static final String AUTHORIZATION = "Authorization";
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_FORM_ENCODED = "application/x-www-form-urlencoded";

    private static final String HEADER_NAV_CALLID = "Nav-Callid";
    private static final String HEADER_NAV_CALL_ID = "nav-call-id";

    private static final Set<String> REST_HEADERS = Set.of(HEADER_NAV_CALLID, HEADER_NAV_CONSUMER_ID, AUTHORIZATION);


    private DefaultRequest() {
        // NOSONAR
    }

    public static HttpRequest.Builder builder() {
        return HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(10))
            .header(HEADER_NAV_CALLID, MDCOperations.getCallId())
            .header(HEADER_NAV_CALL_ID, MDCOperations.getCallId());
    }

    public static HttpRequest.Builder builderForBasicAuth(Supplier<String> authContent) {
        return builder()
            .header(CONTENT_TYPE, APPLICATION_FORM_ENCODED)
            .header(AUTHORIZATION, BASIC_AUTH_HEADER_PREFIX + authContent.get());
    }
    public static HttpRequest.Builder builderForBasicAuth(Supplier<String> authContent, Supplier<String> consumerId) {
        return builder()
            .header(HEADER_NAV_CONSUMER_ID, consumerId.get())
            .header(CONTENT_TYPE, APPLICATION_FORM_ENCODED)
            .header(AUTHORIZATION, BASIC_AUTH_HEADER_PREFIX + authContent.get());
    }

    public static HttpRequest.Builder builderForOidcAuth(Supplier<String> authToken, Supplier<String> consumerId) {
        return builder()
            .header(HEADER_NAV_CONSUMER_ID, consumerId.get())
            .header(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + authToken.get());
    }

    public static HttpRequest.Builder builderForRest(Supplier<String> authToken, Supplier<String> consumerId) {
        return builderForOidcAuth(authToken, consumerId)
            .header(ACCEPT, APPLICATION_JSON);
    }

    public static HttpRequest.Builder builderForConsumerToken(Supplier<String> authToken, Supplier<String> consumerId, Supplier<String> consumerToken) {
        return builderForRest(authToken, consumerId).header(HEADER_NAV_CONSUMER_TOKEN, consumerToken.get());
    }

    public static void validateRestHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(REST_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }

}

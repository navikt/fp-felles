package no.nav.vedtak.klient.http;

import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.function.Supplier;

import no.nav.vedtak.log.mdc.MDCOperations;

public final class DefaultRequest {

    private static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_FORM_ENCODED = "application/x-www-form-urlencoded";

    private static final String HEADER_NAV_CALLID = "Nav-Callid";
    private static final String HEADER_NAV_CALL_ID = "nav-call-id";

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
}

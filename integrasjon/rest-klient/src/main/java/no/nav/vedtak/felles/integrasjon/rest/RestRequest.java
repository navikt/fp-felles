package no.nav.vedtak.felles.integrasjon.rest;

import java.net.http.HttpRequest;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import no.nav.vedtak.klient.http.DefaultRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.token.OidcRequest;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

public final class RestRequest {

    private static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private static final Set<String> REST_HEADERS = Set.of(HEADER_NAV_CONSUMER_ID, HttpHeaders.AUTHORIZATION);


    private RestRequest() {
        // NOSONAR
    }

    public static HttpRequest.Builder builder(SikkerhetContext context) {
        return builder(OidcRequest.tokenFor(context), OidcRequest.consumerIdFor(context));
    }

    public static HttpRequest.Builder builderConsumerToken(SikkerhetContext context) {
        return builder(OidcRequest.tokenFor(context), OidcRequest.consumerIdFor(context))
            .header(HEADER_NAV_CONSUMER_TOKEN, OidcRequest.stsSystemToken().get());
    }

    public static HttpRequest.Builder builderSystemSTS() {
        return builder(OidcRequest.stsSystemToken(), OidcRequest.consumerId());
    }

    public static HttpRequest.Builder builderSystemAzure(String scope) {
        return builder(OidcRequest.azureSystemToken(scope), OidcRequest.consumerId());
    }

    public static void patch(HttpRequest.Builder builder, Object o) {
        var payload = DefaultJsonMapper.toJson(o);
        builder.method("PATCH", HttpRequest.BodyPublishers.ofString(payload));
    }

    public static HttpRequest.BodyPublisher serialiser(Object object) {
        return HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(object));
    }

    private static HttpRequest.Builder builder(Supplier<String> authToken, Supplier<String> consumerId) {
        return DefaultRequest.builder()
            .header(HEADER_NAV_CONSUMER_ID, consumerId.get())
            .header(HttpHeaders.AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + authToken.get())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    }


    static void validateRestHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(REST_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }

}

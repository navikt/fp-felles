package no.nav.vedtak.felles.integrasjon.rest;

import java.net.http.HttpRequest;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import no.nav.vedtak.klient.http.DefaultRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

public class RestRequest {

    private static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";
    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private static final Set<String> REST_HEADERS = Set.of(HEADER_NAV_CONSUMER_ID, HttpHeaders.AUTHORIZATION);

    private static RestRequest REQUEST;

    private final RequestContextSupplier supplier;

    // Local test purposes
    protected RestRequest(RequestContextSupplier supplier) {
        this.supplier = supplier;
    }

    private RestRequest() {
        this(new OidcContextSupplier());
    }

    public static synchronized RestRequest request() {
        var inst= REQUEST;
        if (inst == null) {
            inst = new RestRequest();
            REQUEST = inst;
        }
        return inst;
    }

    public HttpRequest.Builder builder(SikkerhetContext context) {
        return builder(supplier.tokenFor(context), supplier.consumerIdFor(context));
    }

    public HttpRequest.Builder builderConsumerToken(SikkerhetContext context) {
        return builder(supplier.tokenFor(context), supplier.consumerIdFor(context))
            .header(HEADER_NAV_CONSUMER_TOKEN, supplier.consumerToken().get().token());
    }

    public HttpRequest.Builder builderSystemSTS() {
        return builder(supplier.stsSystemToken(), supplier.consumerId());
    }

    public HttpRequest.Builder builderSystemAzure(String scope) {
        return builder(supplier.azureSystemToken(scope), supplier.consumerId());
    }

    public static void patch(HttpRequest.Builder builder, Object o) {
        var payload = DefaultJsonMapper.toJson(o);
        builder.method("PATCH", HttpRequest.BodyPublishers.ofString(payload));
    }

    public static HttpRequest.BodyPublisher serialiser(Object object) {
        return HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(object));
    }

    private static HttpRequest.Builder builder(Supplier<OpenIDToken> authToken, Supplier<String> consumerId) {
        return DefaultRequest.builder()
            .header(HEADER_NAV_CONSUMER_ID, consumerId.get())
            .header(HttpHeaders.AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + authToken.get().token())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
    }


    static void validateRestHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(REST_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import java.net.http.HttpRequest;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import no.nav.vedtak.klient.http.HttpClientRequest;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

/**
 * Encapsulation of java.net.http.HttpRequest to supply OIDC-specific and JSON headers.
 * Supports delayed header-setting and request validation + headers for authorization / basic
 * Methods for serializing objects for use with POST/PUT/PATCH
 *
 * Usage:
 * - Create an ordinary HttpRequest.Builder with URI, Method, and headers specific to the integration.
 * - Create a RestRequest using one of the build methods
 */
public final class RestRequest extends HttpClientRequest {

    private static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String PATCH = "PATCH";
    private static final Set<String> REST_HEADERS = Set.of(HEADER_NAV_CONSUMER_ID, HttpHeaders.AUTHORIZATION);
    private static final RequestContextSupplier CONTEXT_SUPPLIER = new OidcContextSupplier();

    private RestRequest() {
        this(HttpRequest.newBuilder());
    }

    private RestRequest(HttpRequest.Builder builder) {
        super(builder);
        this.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .validator(RestRequest::validateRestHeaders);
    }

    // Web methond PATCH
    public static String patch() {
        return PATCH;
    }

    // Serialize object to json
    public static HttpRequest.BodyPublisher jsonPublisher(Object object) {
        return HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(object));
    }

    public static RestRequest buildFor(Class<?> clazz, HttpRequest.Builder builder) {
        var tokenConfig = RestConfig.tokenConfigFromAnnotation(clazz);
        var scopes = TokenFlow.AZUREAD_CC.equals(tokenConfig) ? RestConfig.scopesFromAnnotation(clazz) : null;
        return build(builder, tokenConfig, scopes);
    }

    public static RestRequest buildFor(HttpRequest.Builder builder, TokenFlow tokenConfig) {
        return build(builder, tokenConfig, null);
    }

    public static RestRequest build(HttpRequest.Builder builder, TokenFlow tokenConfig, String scopes) {
        return switch (tokenConfig) {
            case CONTEXT -> forContext(builder);
            case CONTEXT_ADD_CONSUMER -> forContext(builder).consumerToken();
            case SYSTEM -> requestFor(builder, CONTEXT_SUPPLIER.tokenFor(SikkerhetContext.SYSTEM), CONTEXT_SUPPLIER.consumerId());
            case STS_CC -> requestFor(builder, CONTEXT_SUPPLIER.stsSystemToken(), CONTEXT_SUPPLIER.consumerId());
            case AZUREAD_CC -> requestFor(builder, CONTEXT_SUPPLIER.azureSystemToken(scopes), CONTEXT_SUPPLIER.consumerId());
            default -> throw new IllegalArgumentException("EndpointToken not supported " + tokenConfig.name());
        };
    }

    private RestRequest consumerToken() {
        delayedHeader(HEADER_NAV_CONSUMER_TOKEN, () -> OIDC_AUTH_HEADER_PREFIX + CONTEXT_SUPPLIER.consumerToken().get().token());
        return this;
    }

    private static RestRequest requestFor(HttpRequest.Builder builder, Supplier<OpenIDToken> authToken, Supplier<String> consumerId) {
        return new RestRequest(builder)
            .authorization(authToken)
            .ensureConsumerId(consumerId);
    }

    private static RestRequest forContext(HttpRequest.Builder builder) {
        return new RestRequest(builder)
            .authorization(CONTEXT_SUPPLIER.tokenFor(SikkerhetContext.BRUKER))
            .ensureConsumerId(CONTEXT_SUPPLIER.consumerIdFor(SikkerhetContext.BRUKER));
    }

    private RestRequest authorization(Supplier<OpenIDToken> authToken) {
        delayedHeader(HttpHeaders.AUTHORIZATION, () -> OIDC_AUTH_HEADER_PREFIX + authToken.get().token());
        return this;
    }

    private RestRequest ensureConsumerId(Supplier<String> consumerId) {
        consumerId(consumerId);
        return this;
    }

    private static void validateRestHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(REST_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
        if (request.headers().map().entrySet().stream().filter(e -> REST_HEADERS.contains(e.getKey()))
            .anyMatch(e -> e.getValue() == null || e.getValue().stream().anyMatch(h -> h == null || h.isEmpty()))) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }

}

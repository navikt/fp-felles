package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import no.nav.vedtak.klient.http.HttpClientRequest;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

/**
 * Encapsulation of java.net.http.HttpRequest to supply OIDC-specific and JSON headers.
 * Supports delayed header-setting and request validation + headers for authorization / basic
 * Methods for serializing objects for use with POST/PUT/PATCH
 *
 * Usage:
 * - Create a RestRequest using one of the newRequest methods
 * - Add headers if needed.
 */
public sealed class RestRequest extends HttpClientRequest permits RestRequestExperimental {


    public enum WebMethod {
        GET, POST, PUT, PATCH
    }

    public static record Method(WebMethod restMethod, HttpRequest.BodyPublisher bodyPublisher) {
        public static Method get() {
            return new Method(WebMethod.GET, null);
        }

        public static Method postJson(Object body) {
            return new Method(WebMethod.POST, RestRequest.jsonPublisher(body));
        }
    }

    private static final Map<String, Supplier<String>> DEFAULT_CALLID =
        Map.of(NavHeaders.HEADER_NAV_CALLID, ensureCallId(), NavHeaders.HEADER_NAV_LOWER_CALL_ID, ensureCallId());
    private static final Set<String> VALIDATE_HEADERS = Set.of(NavHeaders.HEADER_NAV_CALLID, NavHeaders.HEADER_NAV_CONSUMER_ID, HttpHeaders.AUTHORIZATION);

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    private static final RequestContextSupplier CONTEXT_SUPPLIER = new OidcContextSupplier();

    private RestRequest() {
        this(HttpRequest.newBuilder(), TokenFlow.CONTEXT, null, CONTEXT_SUPPLIER);
    }

    protected RestRequest(HttpRequest.Builder builder, TokenFlow tokenConfig, String scopes, RequestContextSupplier supplier) {
        super(builder, DEFAULT_CALLID);
        super.timeout(DEFAULT_TIMEOUT);
        super.getBuilder().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        this.authorization(selectTokenSupplier(tokenConfig, scopes, supplier))
            .consumerId(selectConsumerId(tokenConfig, supplier))
            .validator(RestRequest::validateRestHeaders);
        if (TokenFlow.CONTEXT_ADD_CONSUMER.equals(tokenConfig) || TokenFlow.ADAPTIVE_ADD_CONSUMER.equals(tokenConfig)) {
            this.consumerToken(supplier, tokenConfig);
        }
    }

    // Serialize object to json
    public static HttpRequest.BodyPublisher jsonPublisher(Object object) {
        return HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(object));
    }

    public static RestRequest newGET(URI target, Class<?> clazz) {
        return newRequest(Method.get(), target, clazz);
    }

    // Get endpoint form annotation
    public static RestRequest newPOSTJson(Object body, Class<?> clazz) {
        var endpoint = RestConfig.endpointFromAnnotation(clazz);
        return newRequest(Method.postJson(body), endpoint, clazz);
    }

    public static RestRequest newPOSTJson(Object body, URI target, Class<?> clazz) {
        return newRequest(Method.postJson(body), target, clazz);
    }

    public static RestRequest newRequest(Method method, URI target, Class<?> clazz) {
        var tokenConfig = RestConfig.tokenConfigFromAnnotation(clazz);
        var scopes = RestConfig.scopesFromAnnotation(clazz);
        return newRequest(method, target, tokenConfig, scopes);
    }

    public static RestRequest newGET(URI target, TokenFlow tokenConfig, String scopes) {
        return newRequest(Method.get(), target, tokenConfig, scopes);
    }

    public static RestRequest newPOSTJson(Object body, URI target, TokenFlow tokenConfig, String scopes) {
        return newRequest(Method.postJson(body), target, tokenConfig, scopes);
    }

    public static RestRequest newRequest(Method method, URI target, TokenFlow tokenConfig, String scopes) {
        var httpRequestBuilder = getHttpRequestBuilder(method, target);
        return new RestRequest(httpRequestBuilder, tokenConfig, scopes, CONTEXT_SUPPLIER);
    }

    @Override
    public RestRequest timeout(Duration timeout) {
        super.timeout(timeout);
        return this;
    }

    public RestRequest header(String header, String value) {
        super.getBuilder().header(header, value);
        return this;
    }

    @Override
    public RestRequest delayedHeader(String header, Supplier<String> value) {
        super.delayedHeader(header, value);
        return this;
    }

    public RestRequest consumerId(Supplier<String> consumerId) {
        super.delayedHeader(NavHeaders.HEADER_NAV_CONSUMER_ID, consumerId);
        return this;
    }

    public RestRequest consumerId(String consumerId) {
        super.getBuilder().header(NavHeaders.HEADER_NAV_CONSUMER_ID, consumerId);
        return this;
    }

    public RestRequest otherCallId(String header) {
        super.delayedHeader(header, ensureCallId());
        return this;
    }

    @Override
    public RestRequest validator(Consumer<HttpRequest> validator) {
        super.validator(validator);
        return this;
    }

    protected static HttpRequest.Builder getHttpRequestBuilder(Method method, URI target) {
        var builder = HttpRequest.newBuilder(target);
        return switch (method.restMethod()) {
            case GET -> builder.GET();
            case PATCH -> builder.method(method.restMethod().name(), method.bodyPublisher());
            case POST -> builder.POST(method.bodyPublisher());
            case PUT -> builder.PUT(method.bodyPublisher());
        };
    }

    private RestRequest consumerToken(RequestContextSupplier contextSupplier, TokenFlow tokenConfig) {
        if (TokenFlow.ADAPTIVE_ADD_CONSUMER.equals(tokenConfig) && contextSupplier.isAzureContext()) {
            return this;
        }
        delayedHeader(NavHeaders.HEADER_NAV_CONSUMER_TOKEN, () -> OIDC_AUTH_HEADER_PREFIX + contextSupplier.consumerToken().get().token());
        return this;
    }

    protected RestRequest authorization(Supplier<OpenIDToken> authToken) {
        delayedHeader(HttpHeaders.AUTHORIZATION, () -> OIDC_AUTH_HEADER_PREFIX + authToken.get().token());
        return this;
    }

    private static Supplier<String> ensureCallId() {
        return () -> Optional.ofNullable(MDCOperations.getCallId())
            .orElseGet(() -> {
                MDCOperations.putCallId();
                return MDCOperations.getCallId();
            });
    }

    private static Supplier<OpenIDToken> selectTokenSupplier(TokenFlow tokenConfig, String scopes, RequestContextSupplier contextSupplier) {
        return switch (tokenConfig) {
            case ADAPTIVE, ADAPTIVE_ADD_CONSUMER -> contextSupplier.adaptive(SikkerhetContext.BRUKER, scopes);
            case CONTEXT, CONTEXT_ADD_CONSUMER -> contextSupplier.tokenFor(SikkerhetContext.BRUKER);
            case CONTEXT_AZURE -> contextSupplier.azureTokenFor(SikkerhetContext.BRUKER, scopes);
            case SYSTEM, STS_CC -> contextSupplier.tokenFor(SikkerhetContext.SYSTEM);
            case AZUREAD_CC -> contextSupplier.azureTokenFor(SikkerhetContext.SYSTEM, scopes);
        };
    }

    private static Supplier<String> selectConsumerId(TokenFlow tokenConfig, RequestContextSupplier contextSupplier) {
        return switch (tokenConfig) {
            case SYSTEM, STS_CC, AZUREAD_CC -> contextSupplier.consumerIdFor(SikkerhetContext.SYSTEM);
            default -> contextSupplier.consumerIdFor(SikkerhetContext.BRUKER);
        };
    }

    private static void validateRestHeaders(HttpRequest request) {
        if (!request.headers().map().keySet().containsAll(VALIDATE_HEADERS)) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
        if (VALIDATE_HEADERS.stream().anyMatch(header -> request.headers().firstValue(header).filter(h -> !h.isEmpty()).isEmpty())) {
            throw new IllegalArgumentException("Utviklerfeil: mangler headere, fant " + request.headers().map().keySet());
        }
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import no.nav.vedtak.klient.http.HttpClientRequest;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

/**
 * Encapsulation of java.net.http.HttpRequest to supply OIDC-specific and JSON headers.
 * Supports delayed header-setting and request validation + headers for authorization / basic
 * Methods for serializing objects for use with POST/PUT/PATCH
 * <p>
 * Usage:
 * - Create a RestRequest using one of the newRequest methods
 * - Add headers if needed.
 */
public final class RestRequest extends HttpClientRequest {


    public enum WebMethod {
        GET,
        POST,
        PUT,
        PATCH
    }

    public record Method(WebMethod restMethod, HttpRequest.BodyPublisher bodyPublisher) {
        public static Method get() {
            return new Method(WebMethod.GET, null);
        }

        public static Method postJson(Object body) {
            return new Method(WebMethod.POST, RestRequest.jsonPublisher(body));
        }
    }

    private static final Map<String, Supplier<String>> DEFAULT_CALLID = Map.of(NavHeaders.HEADER_NAV_CALLID, ensureCallId(),
        NavHeaders.HEADER_NAV_LOWER_CALL_ID, ensureCallId());
    private static final Set<String> VALIDATE_HEADERS = Set.of(NavHeaders.HEADER_NAV_CALLID, NavHeaders.HEADER_NAV_CONSUMER_ID,
        HttpHeaders.AUTHORIZATION);

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);

    private static final OidcContextSupplier CONTEXT_SUPPLIER = new OidcContextSupplier();

    private RestRequest() {
        this(HttpRequest.newBuilder(), TokenFlow.ADAPTIVE, null);
    }

    private RestRequest(HttpRequest.Builder builder, TokenFlow tokenConfig, String scopes) {
        super(builder, DEFAULT_CALLID);
        super.timeout(DEFAULT_TIMEOUT);
        super.getBuilder().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        this.consumerId(selectConsumerId(tokenConfig));
        if (!TokenFlow.NO_AUTH_NEEDED.equals(tokenConfig)) {
            this.authorization(selectTokenSupplier(tokenConfig, scopes)).validator(RestRequest::validateRestHeaders);
        }
        if (TokenFlow.STS_ADD_CONSUMER.equals(tokenConfig)) {
            this.consumerToken();
        }
    }

    // Serialize object to json
    public static HttpRequest.BodyPublisher jsonPublisher(Object object) {
        return HttpRequest.BodyPublishers.ofString(DefaultJsonMapper.toJson(object));
    }

    public static RestRequest newGET(URI target, RestConfig config) {
        return newRequest(Method.get(), target, config);
    }

    public static RestRequest newPOSTJson(Object body, URI target, RestConfig config) {
        return newRequest(Method.postJson(body), target, config);
    }

    public static RestRequest newRequest(Method method, URI target, RestConfig config) {
        var httpRequestBuilder = getHttpRequestBuilder(method, target);
        return new RestRequest(httpRequestBuilder, config.tokenConfig(), config.scopes());
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

    // For tilfelle med ikke-standard utgående token. Set NO-AUTH og suppler token her
    public RestRequest otherAuthorizationSupplier(Supplier<String> tokenSupplier) {
        super.delayedHeader(HttpHeaders.AUTHORIZATION, () -> OIDC_AUTH_HEADER_PREFIX + tokenSupplier.get())
            .validator(RestRequest::validateRestHeaders);
        return this;
    }

    @Override
    public RestRequest validator(Consumer<HttpRequest> validator) {
        super.validator(validator);
        return this;
    }

    private static HttpRequest.Builder getHttpRequestBuilder(Method method, URI target) {
        var builder = HttpRequest.newBuilder(target);
        return switch (method.restMethod()) {
            case GET -> builder.GET();
            case PATCH -> builder.method(method.restMethod().name(), method.bodyPublisher());
            case POST -> builder.POST(method.bodyPublisher());
            case PUT -> builder.PUT(method.bodyPublisher());
        };
    }

    private RestRequest consumerToken() {
        delayedHeader(NavHeaders.HEADER_NAV_CONSUMER_TOKEN, () -> OIDC_AUTH_HEADER_PREFIX + CONTEXT_SUPPLIER.tokenForSystem(OpenIDProvider.STS, null).get().token());
        return this;
    }

    private RestRequest authorization(Supplier<OpenIDToken> authToken) {
        delayedHeader(HttpHeaders.AUTHORIZATION, () -> OIDC_AUTH_HEADER_PREFIX + authToken.get().token());
        return this;
    }

    private static Supplier<String> ensureCallId() {
        return () -> Optional.ofNullable(MDCOperations.getCallId()).orElseGet(() -> {
            MDCOperations.putCallId();
            return MDCOperations.getCallId();
        });
    }

    private static Supplier<OpenIDToken> selectTokenSupplier(TokenFlow tokenConfig, String scopes) {
        return switch (tokenConfig) {
            case ADAPTIVE -> CONTEXT_SUPPLIER.adaptive(scopes);
            case STS_CC, STS_ADD_CONSUMER -> CONTEXT_SUPPLIER.tokenForSystem(OpenIDProvider.STS, null);
            case AZUREAD_CC -> CONTEXT_SUPPLIER.tokenForSystem(OpenIDProvider.AZUREAD, scopes);
            case NO_AUTH_NEEDED -> throw new IllegalArgumentException("No supplier needed");
        };
    }

    private static Supplier<String> selectConsumerId(TokenFlow tokenConfig) {
        return switch (tokenConfig) {
            case STS_CC, STS_ADD_CONSUMER, AZUREAD_CC -> CONTEXT_SUPPLIER.consumerIdFor(SikkerhetContext.SYSTEM);
            default -> CONTEXT_SUPPLIER.consumerIdForCurrentKontekst();
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

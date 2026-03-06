package no.nav.vedtak.sikkerhet.oidc.token.texas;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.toJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public class TexasTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(TexasTokenKlient.class);
    private static final Environment ENV = Environment.current();
    protected static final String CONTENT_TYPE = "Content-Type";
    protected static final String APPLICATION_JSON = "application/json";
    protected static final String CACHE_CONTROL = "Cache-Control";
    protected static final String NO_CACHE = "no-cache";
    protected static final String PROVIDER_MAA_VAERE_SATT = "provider må være satt";

    private final URI tokenEndpoint;
    private final URI introspectEndpoint;
    private final URI exchangeEndpoint;

    private static volatile TexasTokenKlient INSTANCE; // NOSONAR

    private TexasTokenKlient() {
        this.tokenEndpoint = ENV.getRequiredProperty(TexasProperty.NAIS_TOKEN_ENDPOINT.name(), URI.class);
        this.introspectEndpoint = ENV.getRequiredProperty(TexasProperty.NAIS_TOKEN_INTROSPECTION_ENDPOINT.name(), URI.class);
        this.exchangeEndpoint = ENV.getRequiredProperty(TexasProperty.NAIS_TOKEN_EXCHANGE_ENDPOINT.name(), URI.class);
    }

    // Kun for testing
    TexasTokenKlient(URI tokenEndpoint, URI introspectEndpoint, URI exchangeEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        this.introspectEndpoint = introspectEndpoint;
        this.exchangeEndpoint = exchangeEndpoint;
    }

    public static synchronized TexasTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new TexasTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    public TokenResponse kallTexas(HentTokenRequest req) {
        Objects.requireNonNull(req.identity_provider(), PROVIDER_MAA_VAERE_SATT);
        Objects.requireNonNull(req.target(), "target må være satt");
        LOG.trace("Henter token fra Texas for request {}", req);
        return kallTexas(lagTokenRequest(req), TokenResponse.class);
    }

    private HttpRequest lagTokenRequest(HentTokenRequest tokenRequest) {
        return HttpRequest.newBuilder()
            .header(CACHE_CONTROL, NO_CACHE)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .timeout(Duration.ofSeconds(3))
            .uri(tokenEndpoint)
            .POST(HttpRequest.BodyPublishers.ofString(toJson(tokenRequest), UTF_8))
            .build();
    }

    public TokenResponse exchangeToken(ExchangeTokenRequest req) {
        Objects.requireNonNull(req.identity_provider(), PROVIDER_MAA_VAERE_SATT);
        Objects.requireNonNull(req.target(), "target må være satt");
        Objects.requireNonNull(req.user_token(), "token må være satt");
        LOG.trace("Exchanger token med Texas for request {}", req);
        return kallTexas(lagExchangeRequest(req), TokenResponse.class);
    }

    private HttpRequest lagExchangeRequest(ExchangeTokenRequest exchangeRequest) {
        return HttpRequest.newBuilder()
            .header(CACHE_CONTROL, NO_CACHE)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .timeout(Duration.ofSeconds(3))
            .uri(exchangeEndpoint)
            .POST(HttpRequest.BodyPublishers.ofString(toJson(exchangeRequest), UTF_8))
            .build();
    }

    public IntrospectTokenResponse introspectToken(IntrospectTokenRequest req) {
        Objects.requireNonNull(req.identity_provider(), PROVIDER_MAA_VAERE_SATT);
        Objects.requireNonNull(req.token(), "token må være satt");
        LOG.trace("Introspect token med Texas for request {}", req);
        return kallTexas(lagIntrospectRequest(req), IntrospectTokenResponse.class);
    }

    private HttpRequest lagIntrospectRequest(IntrospectTokenRequest introspectRequest) {
        return HttpRequest.newBuilder()
            .header(CACHE_CONTROL, NO_CACHE)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .timeout(Duration.ofSeconds(3))
            .uri(introspectEndpoint)
            .POST(HttpRequest.BodyPublishers.ofString(toJson(introspectRequest), UTF_8))
            .build();
    }

    private static <T> T kallTexas(HttpRequest request, Class<T> responseType) {
        try (var client = hentEllerByggHttpClient()) { // På sikt vurder å bruke en generell klient eller å cache. De er blitt autocloseable
            var response = client.send(request, HttpResponse.BodyHandlers.ofString(UTF_8));
            if (response == null || response.body() == null || !responskode2xx(response)) {
                throw new TekniskException("F-157385", "Kunne ikke hente token");
            }
            return DefaultJsonMapper.fromJson(response.body(), responseType);
        } catch (JsonProcessingException e) {
            throw new TekniskException("F-208314", "Kunne ikke deserialisere objekt til JSON", e);
        } catch (IOException e) {
            throw new TekniskException("F-432937", "IOException ved kommunikasjon med server", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TekniskException("F-432938", "InterruptedException ved henting av token", e);
        }
    }

    private static boolean responskode2xx(HttpResponse<String> response) {
        var status = response.statusCode();
        return status >= 200 && status < 300;
    }

    private static HttpClient hentEllerByggHttpClient() {
        return HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(2))
            .proxy(HttpClient.Builder.NO_PROXY)
            .build();
    }
}

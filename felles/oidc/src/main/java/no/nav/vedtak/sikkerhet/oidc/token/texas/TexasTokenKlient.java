package no.nav.vedtak.sikkerhet.oidc.token.texas;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.vedtak.mapper.json.DefaultJsonMapper.toJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
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

    /**
     * Metoden returnerer et token basert på informasjonen i requesten. Requesten må inneholde identity_provider og target. Identity_provider kan være ENTRA_ID eller MASKINPORTEN.
     * OBS! Det er kun ENTRA_ID og MASKINPORTEN som støtter token i Texas, og det er kun disse providerene som kan brukes i requesten.
     * @param req request med informasjon om hvilken provider som skal brukes og hvilken target tokenet skal være gyldig for. Må inneholde identity_provider og target.
     * @return TokenResponse som inneholder det hentede tokenet og informasjon om tokenet, som utløpstid og token_type.
     */
    public TokenResponse token(HentTokenRequest req) {
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

    /**
     * Metoden returnerer et nytt token basert på et eksisterende token. Dette kan være nyttig for å få et token med nye claims, eller for å få et token som er gyldig for en annen target.
     * OBS! Det er kun ENTRA_ID og TOKENX som støtter token exchange i Texas, og det er kun disse providerene som kan brukes i requesten.
     * @param req request med informasjon om det eksisterende tokenet og den nye targeten. Må inneholde identity_provider, target og user_token.
     * @return TokenResponse som inneholder det nye tokenet og informasjon om tokenet, som utløpstid og token_type.
     */
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

    /**
     * Metoden returnerer en IntrospectTokenResponse som inneholder informasjon om tokenet, som om det er aktivt
     * @param req request med tokenet som skal introspekteres
     * @return IntrospectTokenResponse som inneholder informasjon om tokenet, inkludert "active" og "error" verdier som kan brukes for å bestemme om tokenet er gyldig eller ikke.
     */
    public IntrospectTokenResponse introspectToken(IntrospectTokenRequest req) {
        Objects.requireNonNull(req.identity_provider(), PROVIDER_MAA_VAERE_SATT);
        Objects.requireNonNull(req.token(), "token må være satt");
        LOG.trace("Introspect token med Texas for request {}", req);
        return kallTexas(lagIntrospectRequest(req), IntrospectTokenResponse.class);
    }

    /**
     * Metoden returnerer en Map<String, Object> med alle claims i tokenet. Dette kan være nyttig for å få tilgang til claims som ikke er standard i IntrospectTokenResponse, eller for å logge alle claims i tokenet.
     * active verdi vil fortelle on tokenet er guyldig eller ikke, og kan brukes i kombinasjon med andre claims for å bestemme om tokenet skal aksepteres eller ikke.
     * error verdi vil fortelle om det var en feil ved introspection, og kan brukes for å logge eller håndtere feil på en bedre måte.
     * @param req request med tokenet som skal introspekteres
     * @return Map<String, Object> med alle claims i tokenet, inkludert "active" og "error" verdier som kan brukes for å bestemme om tokenet er gyldig eller ikke.
     */
    public Map<String, Object> introspectFullToken(IntrospectTokenRequest req) {
        Objects.requireNonNull(req.identity_provider(), PROVIDER_MAA_VAERE_SATT);
        Objects.requireNonNull(req.token(), "token må være satt");
        LOG.trace("Introspect full token med Texas for request {}", req);
        return kallTexas(lagIntrospectRequest(req), Map.class);
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

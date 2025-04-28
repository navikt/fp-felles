package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.vedtak.sikkerhet.oidc.token.impl.Headers.APPLICATION_FORM_ENCODED;
import static no.nav.vedtak.sikkerhet.oidc.token.impl.Headers.CONTENT_TYPE;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.klient.http.ProxyProperty;
import no.nav.vedtak.sikkerhet.oidc.config.MaskinportenProperty;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.util.LRUCache;

public final class MaskinportenTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(MaskinportenTokenKlient.class);

    private static final int RETRIES = 1; // 1 attempt, then n retries

    private static MaskinportenTokenKlient INSTANCE;

    private final LRUCache<String, OpenIDToken> obocache;

    private final URI tokenEndpoint;
    private final List<String> scopes;
    private final URI proxyUrl;
    private final MaskinportenAssertionGenerator assertionGenerator;


    private MaskinportenTokenKlient() {
        var clientId = getMaskinportenProperty(MaskinportenProperty.MASKINPORTEN_CLIENT_ID);
        var privateKey = getMaskinportenProperty(MaskinportenProperty.MASKINPORTEN_CLIENT_JWK);
        var issuer = getMaskinportenProperty(MaskinportenProperty.MASKINPORTEN_ISSUER);
        this.assertionGenerator = new MaskinportenAssertionGenerator(clientId, issuer, privateKey);
        this.tokenEndpoint = URI.create(getMaskinportenProperty(MaskinportenProperty.MASKINPORTEN_TOKEN_ENDPOINT));
        this.scopes = Arrays.stream(getMaskinportenProperty(MaskinportenProperty.MASKINPORTEN_SCOPES)
            .split("\\s+")).toList();
        this.obocache = new LRUCache<>(200, TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES));
        this.proxyUrl = ProxyProperty.getProxyIfFSS();
    }

    public static synchronized MaskinportenTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new MaskinportenTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    public OpenIDToken hentMaskinportenToken(String scope, String resource) {
        if (!scopes.contains(scope)) {
            throw new IllegalStateException("Scope " + scope + " not configured in nais");
        }
        var cacheKey = cacheKey(scope, resource);
        var tokenFromCache = getCachedToken(cacheKey);
        if (tokenFromCache != null && tokenFromCache.isNotExpired()) {
            return tokenFromCache.copy();
        }

        var response = hentToken(assertionGenerator.assertion(scope, resource));
        LOG.debug("Maskinporten fikk token av type {} utløper {}", response.token_type(), response.expires_in());

        var newToken = new OpenIDToken(null, response.token_type(), new TokenString(response.access_token()), scope, response.expires_in());
        putTokenToCache(cacheKey, newToken);
        return newToken.copy();
    }

    private OidcTokenResponse hentToken(String assertion) {
        var request = HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header(CONTENT_TYPE, APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(3))
            .uri(tokenEndpoint)
            .POST(ofFormData(assertion))
            .build();
        return GeneriskTokenKlient.hentTokenRetryable(request, proxyUrl, RETRIES);
    }

    private static HttpRequest.BodyPublisher ofFormData(String assertion) {
        var formdata = "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&"
            + "assertion=" + assertion;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

    private OpenIDToken getCachedToken(String key) {
        return obocache.get(key);
    }

    private void putTokenToCache(String key, OpenIDToken exchangedToken) {
        obocache.put(key, exchangedToken);
    }

    private String cacheKey(String scope, String resource) {
        return scope + ":::" + Optional.ofNullable(resource).orElse("");
    }

    private static String getMaskinportenProperty(MaskinportenProperty property) {
        return Optional.ofNullable(ENV.getProperty(property.name()))
            .orElseGet(() -> ENV.getProperty(property.name().toLowerCase().replace('_', '.')));
    }

}

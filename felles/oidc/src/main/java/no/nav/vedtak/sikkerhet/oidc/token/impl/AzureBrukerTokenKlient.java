package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import no.nav.vedtak.util.LRUCache;

public class AzureBrukerTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureBrukerTokenKlient.class);

    private static volatile AzureBrukerTokenKlient INSTANCE; // NOSONAR

    private LRUCache<String, OpenIDToken> obocache;

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final URI azureProxy;


    private AzureBrukerTokenKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElseThrow();
        this.tokenEndpoint = provider.tokenEndpoint();
        this.azureProxy = provider.proxy();
        this.clientId = provider.clientId();
        this.clientSecret = provider.clientSecret();
        // Justert size. Ser ut som OBO-tokens i dev har varihet på 4100-4600 s.
        this.obocache = new LRUCache<>(2500, TimeUnit.MILLISECONDS.convert(60, TimeUnit.MINUTES));
    }

    public static synchronized AzureBrukerTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new AzureBrukerTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    public OpenIDToken oboExchangeToken(String uid, OpenIDToken incomingToken, String scopes) {
        var tokenFromCache = getCachedToken(uid, scopes);
        if (tokenFromCache != null && tokenFromCache.isNotExpired()) {
            return tokenFromCache.copy();
        }
        var data = "client_id=" + clientId + "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) + "&assertion=" + incomingToken.token()
            + "&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" + "&requested_token_use=on_behalf_of" + "&client_secret=" + clientSecret;
        var request = lagRequest(data);
        LOG.trace("AzureBruker henter token for scope {}", scopes);
        var response = GeneriskTokenKlient.hentToken(request, azureProxy);
        LOG.trace("AzureBruker fikk token for scope {} utløper {}", scopes, response.expires_in());
        if (response.access_token() == null) {
            LOG.warn("AzureBruker tom respons {}", response);
        }

        var newToken = new OpenIDToken(OpenIDProvider.AZUREAD, response.token_type(), new TokenString(response.access_token()), scopes,
            response.expires_in());
        putTokenToCache(uid, scopes, newToken);
        return newToken.copy();
    }

    private HttpRequest lagRequest(String data) {
        return HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(tokenEndpoint)
            .POST(HttpRequest.BodyPublishers.ofString(data, UTF_8))
            .build();
    }

    private OpenIDToken getCachedToken(String uid, String scopes) {
        return obocache.get(cacheKey(uid, scopes));
    }

    private void putTokenToCache(String uid, String scopes, OpenIDToken exchangedToken) {
        obocache.put(cacheKey(uid, scopes), exchangedToken);
    }

    private String cacheKey(String uid, String scopes) {
        return uid + ":::" + scopes;
    }

}

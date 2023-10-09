package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import no.nav.vedtak.sikkerhet.kontekst.DefaultRequestKontekstProvider;
import no.nav.vedtak.sikkerhet.kontekst.KontekstProvider;
import no.nav.vedtak.util.LRUCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public final class TokenXExchangeKlient {

    private static final Logger LOG = LoggerFactory.getLogger(TokenXExchangeKlient.class);
    private static final KontekstProvider KONTEKST_PROVIDER = new DefaultRequestKontekstProvider();
    private static final int RETRIES = 2; // 1 attempt, the n retries

    private static TokenXExchangeKlient INSTANCE;

    private LRUCache<String, OpenIDToken> obocache;

    private final URI tokenEndpoint;


    private TokenXExchangeKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX);
        this.tokenEndpoint = provider.map(OpenIDConfiguration::tokenEndpoint).orElse(null);
        this.obocache = new LRUCache<>(2500, TimeUnit.MILLISECONDS.convert(90, TimeUnit.SECONDS));
    }

    public static synchronized TokenXExchangeKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new TokenXExchangeKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    public OpenIDToken exchangeToken(OpenIDToken token, String assertion, String scopes) {
        var audience = audience(scopes);
        var uid = KONTEKST_PROVIDER.getKontekst().getUid();
        var tokenFromCache = getCachedToken(uid, audience);
        if (tokenFromCache != null && tokenFromCache.isNotExpired()) {
            return tokenFromCache.copy();
        }

        var response = hentToken(token, assertion, audience);
        LOG.debug("TokenX byttet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());

        var newToken = new OpenIDToken(OpenIDProvider.TOKENX, response.token_type(), new TokenString(response.access_token()), audience,
            response.expires_in());
        putTokenToCache(uid, scopes, newToken);
        return newToken.copy();
    }

    private OidcTokenResponse hentToken(OpenIDToken token, String assertion, String audience) {
        var request = HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(tokenEndpoint)
            .POST(ofFormData(token, assertion, audience))
            .build();
        return hentTokenRetryable(request);
    }

    public static OidcTokenResponse hentTokenRetryable(HttpRequest request) {
        int i = RETRIES;
        while (i-- > 0) {
            try {
                return GeneriskTokenKlient.hentToken(request, null);
            } catch (TekniskException e) {
                LOG.info("Feilet {}. gang ved henting av token. Prøver på nytt", RETRIES - i, e);
            }
        }
        return GeneriskTokenKlient.hentToken(request, null);
    }


    private static HttpRequest.BodyPublisher ofFormData(OpenIDToken token, String assertion, String audience) {
        var formdata = "grant_type=urn:ietf:params:oauth:grant-type:token-exchange&"
            + "client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer&"
            + "client_assertion=" + assertion + "&"
            + "subject_token_type=urn:ietf:params:oauth:token-type:jwt&"
            + "subject_token=" + token.token() + "&"
            + "audience=" + audience;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

    private static String audience(String scope) {
        return scope
            .replaceFirst("api://", "")
            .replace("/.default", "")
            .replace(".", ":");
    }

    private OpenIDToken getCachedToken(String uid, String audience) {
        return obocache.get(cacheKey(uid, audience));
    }

    private void putTokenToCache(String uid, String audience, OpenIDToken exchangedToken) {
        obocache.put(cacheKey(uid, audience), exchangedToken);
    }

    private String cacheKey(String uid, String audience) {
        return uid + ":::" + audience;
    }
}

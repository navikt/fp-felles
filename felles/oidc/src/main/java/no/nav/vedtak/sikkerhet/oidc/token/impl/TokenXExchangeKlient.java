package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;

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

    private static TokenXExchangeKlient INSTANCE;

    private final URI tokenEndpoint;


    private TokenXExchangeKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX);
        this.tokenEndpoint = provider.map(OpenIDConfiguration::tokenEndpoint).orElse(null);
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
        var response = hentToken(token, assertion, audience);
        LOG.info("TokenX byttet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.TOKENX, response.token_type(), new TokenString(response.access_token()), audience,
            response.expires_in());
    }

    private OidcTokenResponse hentToken(OpenIDToken token, String assertion, String audience) {
        var request = HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(tokenEndpoint)
            .POST(ofFormData(token, assertion, audience))
            .build();
        try {
            return GeneriskTokenKlient.hentToken(request, null);
        } catch (TekniskException e) {
            //Vist seg å være litt ustabil
            LOG.info("Feiler ved henting av token. Prøver på nytt", e);
            return GeneriskTokenKlient.hentToken(request, null);
        }
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
}

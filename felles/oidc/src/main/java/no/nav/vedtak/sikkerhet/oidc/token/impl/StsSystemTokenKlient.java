package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.http.HttpRequest;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class StsSystemTokenKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(StsSystemTokenKlient.class);

    private static final String SCOPE = "openid";

    private static final OpenIDConfiguration OIDCONFIG = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.STS).orElseThrow();

    private static OpenIDToken accessToken;

    public static synchronized OpenIDToken hentAccessToken() {
        if (accessToken != null && accessToken.isNotExpired()) {
            return accessToken.copy();
        }
        var response = hentToken();
        LOG.info("STS hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        accessToken = new OpenIDToken(OpenIDProvider.STS, response.token_type(),
            new TokenString(response.access_token()), SCOPE, response.expires_in());
        return accessToken.copy();
    }

    private static OidcTokenResponse hentToken() {
        var request = HttpRequest.newBuilder()
            .header(Headers.AUTHORIZATION, Headers.basicCredentials(OIDCONFIG.clientId(), OIDCONFIG.clientSecret()))
            .header("Nav-Consumer-Id", OIDCONFIG.clientId())
            .header("Nav-Call-Id", MDCOperations.getCallId())
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(OIDCONFIG.tokenEndpoint())
            .POST(ofFormData())
            .build();
        return GeneriskTokenKlient.hentToken(request, null);
    }

    private static HttpRequest.BodyPublisher ofFormData() {
        var formdata = "grant_type=client_credentials&scope=" + SCOPE;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

}

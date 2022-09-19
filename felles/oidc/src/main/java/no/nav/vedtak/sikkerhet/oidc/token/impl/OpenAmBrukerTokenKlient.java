package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class OpenAmBrukerTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenAmBrukerTokenKlient.class);

    private static final String SCOPE = "openid";
    private static final String REALM = "/";

    private static final OpenIDConfiguration CONFIGURATION = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.ISSO).orElseThrow();

    public static OpenIDToken exhangeAuthCode(String authorizationCode, String callback) {
        String data = "grant_type=authorization_code" +
            "&realm=" + REALM +
            "&redirect_uri=" + URLEncoder.encode(callback, UTF_8) +
            "&code=" + authorizationCode;
        var request = lagRequest(CONFIGURATION.clientId(), data);
        var response = GeneriskTokenKlient.hentToken(request, null);
        LOG.info("ISSO hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.ISSO, response.token_type(), new TokenString(response.id_token()),
            SCOPE, new TokenString(response.refresh_token()), response.expires_in());

    }

    public static Optional<OpenIDToken> refreshIdToken(OpenIDToken expiredToken, String clientName) {
        if (expiredToken.refreshToken().isEmpty())
            return Optional.empty();
        var data = "grant_type=refresh_token" +
            "&scope=openid" +
            "&realm=" + REALM +
            "&refresh_token=" + expiredToken.refreshToken().get();
        var request = lagRequest(clientName, data);
        var response = GeneriskTokenKlient.hentToken(request, null);
        LOG.info("ISSO hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        if (response.token_type() == null || response.expires_in() == null) {
            return Optional.empty();
        }
        var token = new OpenIDToken(OpenIDProvider.ISSO, response.token_type(), new TokenString(response.id_token()),
            SCOPE, new TokenString(response.refresh_token()), response.expires_in());
        return Optional.of(token);
    }

    private static HttpRequest lagRequest(String clientName, String data) {
        return  HttpRequest.newBuilder()
            .header(Headers.AUTHORIZATION, Headers.basicCredentials(clientName, CONFIGURATION.clientSecret()))
            .header("Nav-Consumer-Id", CONFIGURATION.clientId())
            .header("Nav-Call-Id", MDCOperations.getCallId())
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(CONFIGURATION.tokenEndpoint())
            .POST(HttpRequest.BodyPublishers.ofString(data, UTF_8))
            .build();
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes(UTF_8));
    }
}

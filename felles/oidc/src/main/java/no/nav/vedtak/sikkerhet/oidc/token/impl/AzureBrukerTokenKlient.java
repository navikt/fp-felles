package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class AzureBrukerTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureBrukerTokenKlient.class);

    private static final OpenIDConfiguration CONFIGURATION = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElse(null);

    public static OpenIDToken exhangeAuthCode(String authorizationCode, String callback, String scopes) {
        String data = "client_id=" + CONFIGURATION.clientId() +
            "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
            "&code=" + authorizationCode +
            "&redirect_uri=" + URLEncoder.encode(callback, UTF_8) +
            "&grant_type=authorization_code" +
            //"&code_verifier=" + "ThisIsntRandomButItNeedsToBe43CharactersLong" +
            "&client_secret=" + CONFIGURATION.clientSecret();
        var request = lagRequest(data);
        var response = GeneriskTokenKlient.hentToken(request, CONFIGURATION.proxy());
        LOG.info("ISSO hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.ISSO, response.token_type(), new TokenString(response.id_token()),
            scopes, new TokenString(response.refresh_token()), response.expires_in());

    }

    public static Optional<OpenIDToken> refreshIdToken(OpenIDToken expiredToken, String scopes) {
        if (expiredToken.refreshToken().isEmpty())
            return Optional.empty();
        //client_id=535fb089-9ff3-47b6-9bfb-4f1264799865
        //&scope=https%3A%2F%2Fgraph.microsoft.com%2Fmail.read
        //&refresh_token=OAAABAAAAiL9Kn2Z27UubvWFPbm0gLWQJVzCTE9UkP3pSx1aXxUjq...
        //&grant_type=refresh_token
        //&client_secret=sampleCredentia1s
        var data = "client_id=" + CONFIGURATION.clientId() +
            "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
            "&refresh_token=" + expiredToken.refreshToken().get() +
            "&grant_type=refresh_token" +
            "&client_secret=" + CONFIGURATION.clientSecret();
        var request = lagRequest(data);
        var response = GeneriskTokenKlient.hentToken(request, CONFIGURATION.proxy());
        LOG.info("ISSO hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        if (response.token_type() == null || response.expires_in() == null) {
            return Optional.empty();
        }
        var token = new OpenIDToken(OpenIDProvider.ISSO, response.token_type(), new TokenString(response.id_token()),
            scopes, new TokenString(response.refresh_token()), response.expires_in());
        return Optional.of(token);
    }

    private static HttpRequest lagRequest(String data) {
        return  HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header("Content-type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(10))
            .uri(CONFIGURATION.tokenEndpoint())
            .POST(HttpRequest.BodyPublishers.ofString(data, UTF_8))
            .build();
    }

}

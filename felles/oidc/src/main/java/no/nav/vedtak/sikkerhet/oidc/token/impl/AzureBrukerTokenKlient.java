package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class AzureBrukerTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureBrukerTokenKlient.class);

    private static volatile AzureBrukerTokenKlient INSTANCE; // NOSONAR

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final URI azureProxy;


    public AzureBrukerTokenKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElseThrow();
        this.tokenEndpoint = provider.tokenEndpoint();
        this.azureProxy = provider.proxy();
        this.clientId = provider.clientId();
        this.clientSecret = provider.clientSecret();
    }

    public static synchronized AzureBrukerTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new AzureBrukerTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    public OpenIDToken exhangeAuthCode(String authorizationCode, String callback, String scopes) {
        String data = "client_id=" + clientId +
            "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
            "&code=" + authorizationCode +
            "&redirect_uri=" + URLEncoder.encode(callback, UTF_8) +
            "&grant_type=authorization_code" +
            //"&code_verifier=" + "ThisIsntRandomButItNeedsToBe43CharactersLong" +
            "&client_secret=" + clientSecret;
        var request = lagRequest(data);
        var response = GeneriskTokenKlient.hentToken(request, azureProxy);
        LOG.info("AzureBruker hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.AZUREAD, response.token_type(), new TokenString(response.id_token()),
            scopes, new TokenString(response.refresh_token()), response.expires_in());

    }

    public Optional<OpenIDToken> refreshIdToken(OpenIDToken expiredToken, String scopes) {
        if (expiredToken.refreshToken().isEmpty())
            return Optional.empty();
        var data = "client_id=" + clientId +
            "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
            "&refresh_token=" + expiredToken.refreshToken().get() +
            "&grant_type=refresh_token" +
            "&client_secret=" + clientSecret;
        var request = lagRequest(data);
        var response = GeneriskTokenKlient.hentToken(request, azureProxy);
        LOG.info("AzureBruker hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        if (response.token_type() == null || response.expires_in() == null) {
            return Optional.empty();
        }
        var token = new OpenIDToken(OpenIDProvider.AZUREAD, response.token_type(), new TokenString(response.id_token()),
            scopes, new TokenString(response.refresh_token()), response.expires_in());
        return Optional.of(token);
    }

    public OpenIDToken oboExchangeToken(OpenIDToken incomingToken, String scopes) {
        // TODO: vurder caching av incoming+scopes -> exchanged
        var data = "client_id=" + clientId +
            "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
            "&assertion=" + incomingToken.token() +
            "&grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" +
            "&requested_token_use=on_behalf_of" +
            "&client_secret=" + clientSecret;
        var request = lagRequest(data);
        var response = GeneriskTokenKlient.hentToken(request, azureProxy);
        LOG.info("AzureBruker hentet og fikk token av type {} utløper {}", response.token_type(), response.expires_in());
        return new OpenIDToken(OpenIDProvider.AZUREAD, response.token_type(), new TokenString(response.access_token()),
            scopes, new TokenString(response.refresh_token()), response.expires_in());
    }

    private HttpRequest lagRequest(String data) {
        return  HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header("Content-type", "application/x-www-form-urlencoded")
            .timeout(Duration.ofSeconds(10))
            .uri(tokenEndpoint)
            .POST(HttpRequest.BodyPublishers.ofString(data, UTF_8))
            .build();
    }

}
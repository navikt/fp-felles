package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;


public class AzureSystemTokenKlient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureSystemTokenKlient.class);

    private static AzureSystemTokenKlient INSTANCE;

    private final URI tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final URI azureProxy;

    private final Map<String, OpenIDToken> accessToken = new LinkedHashMap<>();

    public static synchronized AzureSystemTokenKlient instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new AzureSystemTokenKlient();
            INSTANCE = inst;
        }
        return inst;
    }

    private AzureSystemTokenKlient() {
        var provider = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElseThrow();
        this.tokenEndpoint = provider.tokenEndpoint();
        this.azureProxy = provider.proxy();
        this.clientId = provider.clientId();
        this.clientSecret = provider.clientSecret();
    }

    public synchronized OpenIDToken hentAccessToken(String scope) {
        // Expiry normalt 3599 ...
        var heldToken = accessToken.get(scope);
        if (heldToken != null && heldToken.isNotExpired()) {
            return heldToken.copy();
        }
        var response = hentAccessToken(clientId, clientSecret, tokenEndpoint, azureProxy, scope);
        LOG.info("AzureAD hentet token for scope {} fikk token av type {} utløper {}", scope, response.token_type(), response.expires_in());
        var newToken = new OpenIDToken(OpenIDProvider.AZUREAD, response.token_type(), new TokenString(response.access_token()), scope,
            response.expires_in());
        accessToken.put(scope, newToken);
        return newToken.copy();
    }


    private static OidcTokenResponse hentAccessToken(String clientId, String clientSecret, URI tokenEndpoint, URI proxy, String scope) {
        var request = HttpRequest.newBuilder()
            .header("Cache-Control", "no-cache")
            .header(Headers.CONTENT_TYPE, Headers.APPLICATION_FORM_ENCODED)
            .timeout(Duration.ofSeconds(10))
            .uri(tokenEndpoint)
            .POST(ofFormData(clientId, clientSecret, scope))
            .build();
        return GeneriskTokenKlient.hentToken(request, proxy);
    }

    private static HttpRequest.BodyPublisher ofFormData(String clientId, String clientSecret, String scope) {
        var encodedScope = URLEncoder.encode(scope, UTF_8);
        var formdata = "grant_type=client_credentials" + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + encodedScope;
        return HttpRequest.BodyPublishers.ofString(formdata, UTF_8);
    }

}

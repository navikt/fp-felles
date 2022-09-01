package no.nav.vedtak.isso.ressurs;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class AzureAuthorizationRequestBuilder {

    private static final SecureRandom RND = new SecureRandom();

    private static final String SCOPE = "openid";

    private String stateIndex;

    public AzureAuthorizationRequestBuilder() {

        byte[] bytes = new byte[20];
        RND.nextBytes(bytes);
        stateIndex = "state_" + new BigInteger(1, bytes).toString(16);
    }

    public String getStateIndex() {
        return stateIndex;
    }

    public String buildRedirectString() {
        var scopes = AzureConfigProperties.getAzureScopes()
            .map(s -> s.replace("\\*", " "))
            .orElse(SCOPE);
        var providerConfig = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.AZUREAD).orElseThrow();
        var clientId = providerConfig.clientId();
        var redirectUrl = ServerInfo.instance().getCallbackUrl();

        //TODO - sjekke ut PKCE
        //&code_challenge=YTFjNj......
        //&code_challenge_method=S256

        // KCD?  "?session=winssochain&authIndexType=service&authIndexValue=winssochain";
        return providerConfig.authorizationEndpoint().toString() +
            "&response_type=code" +
            "&response_mode=query" +
            "&scope=" + scopes +
            "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
            "&state="+ getStateIndex() +
            "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
    }

}

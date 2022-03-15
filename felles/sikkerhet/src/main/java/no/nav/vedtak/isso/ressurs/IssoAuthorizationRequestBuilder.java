package no.nav.vedtak.isso.ressurs;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class IssoAuthorizationRequestBuilder {

    private static final SecureRandom RND = new SecureRandom();

    private static final String SCOPE = "openid";

    private String stateIndex;

    public IssoAuthorizationRequestBuilder() {

        byte[] bytes = new byte[20];
        RND.nextBytes(bytes);
        stateIndex = "state_" + new BigInteger(1, bytes).toString(16);
    }

    public String getStateIndex() {
        return stateIndex;
    }

    public String buildRedirectString() {
        var providerConfig = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.ISSO).orElseThrow();
        var clientId = providerConfig.clientId();
        var redirectUrl = ServerInfo.instance().getCallbackUrl();
        var kerberos = "?session=winssochain&authIndexType=service&authIndexValue=winssochain";
        return providerConfig.authorizationEndpoint().toString() + kerberos +
            "&response_type=code" +
            "&scope=" + SCOPE +
            "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
            "&state="+ getStateIndex() +
            "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
    }

}

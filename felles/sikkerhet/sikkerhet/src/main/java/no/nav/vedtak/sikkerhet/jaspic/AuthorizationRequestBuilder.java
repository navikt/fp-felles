package no.nav.vedtak.sikkerhet.jaspic;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.isso.config.ServerInfo;

public class AuthorizationRequestBuilder {

    private static final SecureRandom RND = new SecureRandom();

    private static final String SCOPE = "openid";

    private boolean useKerberos = true;
    private String stateIndex;

    public AuthorizationRequestBuilder() {

        byte[] bytes = new byte[20];
        RND.nextBytes(bytes);
        stateIndex = "state_" + new BigInteger(1, bytes).toString(16);
    }

    public AuthorizationRequestBuilder ikkeBrukKerberos() {
        useKerberos = false;
        return this;
    }

    public String getStateIndex() {
        return stateIndex;
    }

    public String buildRedirectString() {
        String clientId = OpenAMHelper.getIssoUserName();
        String state = stateIndex;
        String redirectUrl = ServerInfo.instance().getCallbackUrl();
        String kerberosTrigger = useKerberos
                ? "session=winssochain&authIndexType=service&authIndexValue=winssochain&"
                : "";
        return String.format("%s/authorize?%sresponse_type=code&scope=%s&client_id=%s&state=%s&redirect_uri=%s",
                OpenAMHelper.getIssoHostUrl(),
                kerberosTrigger,
                SCOPE,
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                state,
                URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
    }

}

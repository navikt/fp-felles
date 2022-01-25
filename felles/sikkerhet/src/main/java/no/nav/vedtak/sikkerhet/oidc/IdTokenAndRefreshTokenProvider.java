package no.nav.vedtak.sikkerhet.oidc;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.context.containers.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.context.containers.OidcCredential;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class IdTokenAndRefreshTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(IdTokenAndRefreshTokenProvider.class);

    public IdTokenAndRefreshToken getToken(String authorizationCode) {
        return TokenProviderUtil.getToken(() -> createTokenRequest(authorizationCode), this::extractToken);
    }

    private HttpRequestBase createTokenRequest(String authorizationCode) {
        var providerConfig = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.ISSO).orElseThrow();
        var urlEncodedRedirectUri = URLEncoder.encode(ServerInfo.instance().getCallbackUrl(), UTF_8);

        String realm = "/";
        HttpPost request = new HttpPost(providerConfig.tokenEndpoint());
        request.reset();
        request.setHeader("Authorization", TokenProviderUtil.basicCredentials(providerConfig.clientId(), providerConfig.clientSecret()));
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Content-type", "application/x-www-form-urlencoded");
        String data = "grant_type=authorization_code"
                + "&realm=" + realm
                + "&redirect_uri=" + urlEncodedRedirectUri
                + "&code=" + authorizationCode;
        LOG.debug("Requesting tokens by POST to {}", LoggerUtils.removeLineBreaks(providerConfig.tokenEndpoint().toString()));
        request.setEntity(new StringEntity(data, UTF_8));
        return request;
    }

    private IdTokenAndRefreshToken extractToken(String responseString) {
        OidcCredential token = new OidcCredential(TokenProviderUtil.findToken(responseString, "id_token"));
        String refreshToken = TokenProviderUtil.findToken(responseString, "refresh_token");
        return new IdTokenAndRefreshToken(token, refreshToken);
    }

}

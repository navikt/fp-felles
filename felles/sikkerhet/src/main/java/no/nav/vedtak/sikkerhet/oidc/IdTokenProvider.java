package no.nav.vedtak.sikkerhet.oidc;

import java.util.Optional;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public class IdTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(IdTokenProvider.class);

    public Optional<OidcTokenHolder> getToken(OidcTokenHolder idToken, String refreshToken) {
        String oidcClientName = JwtUtil.getClientName(idToken.token());
        LOG.debug("Refreshing token, using client name {}", LoggerUtils.removeLineBreaks(oidcClientName)); // NOSONAR CRLF håndtert
        Optional<String> newToken = TokenProviderUtil.getTokenOptional(() -> createTokenRequest(oidcClientName, refreshToken),
                s -> TokenProviderUtil.findToken(s, "id_token"));
        return newToken.map(s -> new OidcTokenHolder(s, idToken.fromCookie()));
    }

    private HttpRequestBase createTokenRequest(String oidcClientName, String refreshToken) {
        var providerConfig = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.ISSO).orElseThrow();
        var tokenEndpoint = providerConfig.tokenEndpoint();
        String realm = "/";
        String password = providerConfig.clientSecret();

        HttpPost request = new HttpPost(tokenEndpoint);
        request.setHeader("Authorization", TokenProviderUtil.basicCredentials(oidcClientName, password));
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Content-type", "application/x-www-form-urlencoded");
        String data = "grant_type=refresh_token"
                + "&scope=openid"
                + "&realm=" + realm
                + "&refresh_token=" + refreshToken;
        LOG.debug("Refreshing ID-token by POST to {}", LoggerUtils.removeLineBreaks(tokenEndpoint.toString())); // NOSONAR CRLF håndtert
        request.setEntity(new StringEntity(data, "UTF-8"));
        return request;
    }

}

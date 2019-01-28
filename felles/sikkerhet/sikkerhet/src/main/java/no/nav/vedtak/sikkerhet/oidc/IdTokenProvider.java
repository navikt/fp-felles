package no.nav.vedtak.sikkerhet.oidc;

import java.util.Optional;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

public class IdTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(IdTokenProvider.class);

    public Optional<OidcTokenHolder> getToken(OidcTokenHolder idToken, String refreshToken) {
        String oidcClientName = JwtUtil.getClientName(idToken.getToken());
        log.debug("Refreshing token, using client name {}", LoggerUtils.removeLineBreaks(oidcClientName)); //NOSONAR CRLF håndtert
        Optional<String> newToken = TokenProviderUtil.getTokenOptional(() -> createTokenRequest(oidcClientName, refreshToken), s -> TokenProviderUtil.findToken(s, "id_token"));
        return newToken.map(s -> new OidcTokenHolder(s, idToken.isFromCookie()));
    }

    private HttpRequestBase createTokenRequest(String oidcClientName, String refreshToken) {
        String host = OpenAMHelper.getIssoHostUrl();
        String realm = "/";
        String password = OpenAMHelper.getIssoPassword();

        HttpPost request = new HttpPost(host + "/access_token");
        request.setHeader("Authorization", TokenProviderUtil.basicCredentials(oidcClientName, password));
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Content-type", "application/x-www-form-urlencoded");
        String data = "grant_type=refresh_token"
                + "&scope=openid"
                + "&realm=" + realm
                + "&refresh_token=" + refreshToken;
        log.debug("Refreshing ID-token by POST to {}", LoggerUtils.removeLineBreaks(host)); //NOSONAR CRLF håndtert
        request.setEntity(new StringEntity(data, "UTF-8"));
        return request;
    }

}

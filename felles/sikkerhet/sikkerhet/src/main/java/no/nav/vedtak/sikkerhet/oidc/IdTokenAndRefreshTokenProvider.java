package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class IdTokenAndRefreshTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(IdTokenAndRefreshTokenProvider.class);

    private final String host = OpenAMHelper.getIssoHostUrl();
    private final String username = OpenAMHelper.getIssoUserName();
    private final String password = OpenAMHelper.getIssoPassword();

    public IdTokenAndRefreshToken getToken(String authorizationCode, UriInfo redirectUri) {
        return TokenProviderUtil.getToken(() -> createTokenRequest(authorizationCode, redirectUri.getBaseUri()), this::extractToken);
    }

    public IdTokenAndRefreshToken getToken(String authorizationCode, URI redirectUri) {
        return TokenProviderUtil.getToken(() -> createTokenRequest(authorizationCode, redirectUri), this::extractToken);
    }

    private HttpRequestBase createTokenRequest(String authorizationCode, URI redirectUri) {
        String urlEncodedRedirectUri;
        try {
            String url = ServerInfo.instance().getCallbackUrl();
            urlEncodedRedirectUri = URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            //gjør om fra UriInfo til string, hvis ikke blir det problemer for testen som tester unikhet av feil
            //fordi UriInfo ikke er på classpath for testen
            throw TokenProviderFeil.FACTORY.kunneIkkeUrlEncodeRedirectUri(redirectUri.toString(), e).toException();
        }

        String realm = "/";
        HttpPost request = new HttpPost(host + "/access_token");
        request.reset();
        request.setHeader("Authorization", TokenProviderUtil.basicCredentials(username, password));
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Content-type", "application/x-www-form-urlencoded");
        String data = "grant_type=authorization_code"
                + "&realm=" + realm
                + "&redirect_uri=" + urlEncodedRedirectUri
                + "&code=" + authorizationCode;
        log.debug("Requesting tokens by POST to {}", LoggerUtils.removeLineBreaks(host)); //NOSONAR
        request.setEntity(new StringEntity(data, "UTF-8"));
        return request;
    }

    private IdTokenAndRefreshToken extractToken(String responseString) {
        OidcCredential token = new OidcCredential(TokenProviderUtil.findToken(responseString, "id_token"));
        String refreshToken = TokenProviderUtil.findToken(responseString, "refresh_token");
        return new IdTokenAndRefreshToken(token, refreshToken);
    }

}

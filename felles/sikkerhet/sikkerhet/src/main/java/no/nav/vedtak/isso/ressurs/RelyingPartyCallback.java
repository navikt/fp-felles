package no.nav.vedtak.isso.ressurs;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.oidc.JwtUtil.getIssuser;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;
import no.nav.vedtak.sikkerhet.oidc.IdTokenAndRefreshTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider;

@Path("")
public class RelyingPartyCallback {
    private static final Logger LOG = LoggerFactory.getLogger(RelyingPartyCallback.class);

    private IdTokenAndRefreshTokenProvider tokenProvider = new IdTokenAndRefreshTokenProvider();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogin(@QueryParam("code") String authorizationCode, @QueryParam("state") String state, @Context HttpHeaders headers) {
        if (authorizationCode == null) {
            RelyingPartyCallbackFeil.manglerCodeParameter().log(LOG);
            return status(BAD_REQUEST).build();
        }
        if (state == null) {
            RelyingPartyCallbackFeil.manglerStateParameter().log(LOG);
            return status(BAD_REQUEST).build();
        }

        Cookie redirect = headers.getCookies().get(state);
        if (redirect == null || redirect.getValue() == null || redirect.getValue().isEmpty()) {
            RelyingPartyCallbackFeil.manglerCookieForRedirectionURL().log(LOG);
            return status(BAD_REQUEST).build();
        }

        var tokens = tokenProvider.getToken(authorizationCode);
        var token = tokens.getIdToken().getToken();
        if (!OidcTokenValidatorProvider.instance().getValidator(getIssuser(token)).validate(new OidcTokenHolder(token, false)).isValid()) {
            return status(FORBIDDEN).build();
        }

        boolean sslOnlyCookie = ServerInfo.instance().isUsingTLS();
        String cookieDomain = ServerInfo.instance().getCookieDomain();
        String cookiePath = ServerInfo.instance().getCookiePath();
        var tokenCookie = new NewCookie(ID_TOKEN_COOKIE_NAME, token, cookiePath, cookieDomain, "", NewCookie.DEFAULT_MAX_AGE,
                sslOnlyCookie,
                true);
        var refreshTokenCookie = new NewCookie(REFRESH_TOKEN_COOKIE_NAME, tokens.getRefreshToken(), cookiePath, cookieDomain, "",
                NewCookie.DEFAULT_MAX_AGE,
                sslOnlyCookie, true);
        var deleteOldStateCookie = new NewCookie(state, "", "/", null, "", 0, sslOnlyCookie, true);

        // TODO (u139158): CSRF attack protection. See RFC-6749 section 10.12 (the
        // state-cookie containing redirectURL shold be encrypted to avoid tampering)
        var responseBuilder = Response.temporaryRedirect(URI.create(decode(redirect.getValue(), UTF_8)));
        responseBuilder.cookie(tokenCookie);
        responseBuilder.cookie(refreshTokenCookie);
        responseBuilder.cookie(deleteOldStateCookie);
        responseBuilder.cacheControl(noCache());
        return responseBuilder.build();
    }

    private static CacheControl noCache() {
        CacheControl cc = new CacheControl();
        cc.setMustRevalidate(true);
        cc.setPrivate(true);
        cc.setNoCache(true);
        cc.setNoStore(true);
        return cc;
    }

}

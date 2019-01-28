package no.nav.vedtak.isso.ressurs;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.filter.DoNotCache;
import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.domene.IdTokenAndRefreshToken;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;
import no.nav.vedtak.sikkerhet.oidc.IdTokenAndRefreshTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidator;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorResult;

@Path("")
@DoNotCache
public class RelyingPartyCallback {
    private static final Logger log = LoggerFactory.getLogger(RelyingPartyCallback.class);

    private IdTokenAndRefreshTokenProvider tokenProvider = new IdTokenAndRefreshTokenProvider();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogin(@QueryParam("code") String authorizationCode, @QueryParam("state") String state, @Context UriInfo uri, @Context HttpHeaders headers) {
        if (authorizationCode == null) {
            RelyingPartyCallbackFeil.FACTORY.manglerCodeParameter().log(log);
            return Response.status(BAD_REQUEST).build();
        }
        if (state == null) {
            RelyingPartyCallbackFeil.FACTORY.manglerStateParameter().log(log);
            return Response.status(BAD_REQUEST).build();
        }

        Cookie redirect = headers.getCookies().get(state);
        if (redirect == null || redirect.getValue() == null || redirect.getValue().isEmpty()) {
            RelyingPartyCallbackFeil.FACTORY.manglerCookieForRedirectionURL().log(log);
            return Response.status(BAD_REQUEST).build();
        }

        IdTokenAndRefreshToken tokens = tokenProvider.getToken(authorizationCode, uri);
        OidcCredential token = tokens.getIdToken();
        String refreshToken = tokens.getRefreshToken();

        String issuser = JwtUtil.getIssuser(token.getToken());
        OidcTokenValidator validator = OidcTokenValidatorProvider.instance().getValidator(issuser);
        OidcTokenValidatorResult result = validator.validate(new OidcTokenHolder(token.getToken(), false));

        if (!result.isValid()) {
            return Response.status(FORBIDDEN).build();
        }

        boolean sslOnlyCookie = ServerInfo.instance().isUsingTLS();
        String cookieDomain = ServerInfo.instance().getCookieDomain();
        NewCookie tokenCookie = new NewCookie(ID_TOKEN_COOKIE_NAME, token.getToken(), "/", cookieDomain, "", NewCookie.DEFAULT_MAX_AGE, sslOnlyCookie, true);
        NewCookie refreshTokenCookie = new NewCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, "/", cookieDomain, "", NewCookie.DEFAULT_MAX_AGE, sslOnlyCookie, true);
        NewCookie deleteOldStateCookie = new NewCookie(state, "", "/", null, "", 0, sslOnlyCookie, true);

        Response.ResponseBuilder responseBuilder;
        //TODO (u139158): CSRF attack protection. See RFC-6749 section 10.12 (the state-cookie containing redirectURL shold be encrypted to avoid tampering)
        String originalUrl = urlDecode(redirect.getValue());
        responseBuilder = Response.temporaryRedirect(URI.create(originalUrl));
        responseBuilder.cookie(tokenCookie);
        responseBuilder.cookie(refreshTokenCookie);
        responseBuilder.cookie(deleteOldStateCookie);
        return responseBuilder.build();
    }


    private static String urlDecode(String urlEncoded) {
        try {
            return URLDecoder.decode(urlEncoded, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw RelyingPartyCallbackFeil.FACTORY.kunneIkkeUrlDecode(urlEncoded, e).toException();
        }
    }

}

package no.nav.vedtak.isso.ressurs;

import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.NewCookie.DEFAULT_MAX_AGE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.status;
import static javax.ws.rs.core.Response.temporaryRedirect;
import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.isso.oidc.AzureADTokenProvider;
import no.nav.vedtak.isso.oidc.OpenAmTokenProvider;
import no.nav.vedtak.sikkerhet.oidc.OidcTokenValidatorConfig;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

@Path("")
public class RelyingPartyCallback {
    private static final Logger LOG = LoggerFactory.getLogger(RelyingPartyCallback.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogin(@QueryParam("code") String authorizationCode, @QueryParam("state") String state, @Context HttpHeaders headers, @Context HttpServletRequest httpServletRequest) {
        if (authorizationCode == null) {
            LOG.warn("Mangler parameter 'code' i URL");
            return status(BAD_REQUEST).build();
        }
        if (state == null) {
            LOG.warn("Mangler parameter 'state' i URL");
            return status(BAD_REQUEST).build();
        }

        var redirect = headers.getCookies().get(state);
        if (redirect == null || redirect.getValue() == null || redirect.getValue().isEmpty()) {
            LOG.warn("Cookie for redirect URL mangler eller er tom");
            return status(BAD_REQUEST).build();
        }

        OpenIDToken token;
        if (AzureConfigProperties.isAzureEnabled() && matcherAzureDomain(httpServletRequest)) {
            token = AzureADTokenProvider.exhangeAzureAuthCode(authorizationCode, AzureConfigProperties.getAzureCallback());
            if (!OidcTokenValidatorConfig.instance().getValidator(OpenIDProvider.AZUREAD).validate(token.primary()).isValid()) {
                return status(FORBIDDEN).build();
            }
        } else {
            token = new OpenAmTokenProvider().exhangeOpenAmAuthCode(authorizationCode, ServerInfo.instance().getCallbackUrl());
            if (!OidcTokenValidatorConfig.instance().getValidator(OpenIDProvider.ISSO).validate(token.primary()).isValid()) {
                return status(FORBIDDEN).build();
            }
        }

        var builder = temporaryRedirect(URI.create(decode(redirect.getValue(), UTF_8)));

        // rydd krims-krams cookies
        cleanCookieJar(builder, headers);
        // definitiv wipe angitt state cookie param
        wipeStateCookie(builder, redirect.getName(), redirect.getPath(), redirect.getDomain());

        boolean sslOnlyCookie = ServerInfo.instance().isUsingTLS();
        String cookieDomain = ServerInfo.instance().getCookieDomain();
        String cookiePath = ServerInfo.instance().getCookiePath();

        var tokenCookie = new NewCookie(ID_TOKEN_COOKIE_NAME, token.token(), cookiePath, cookieDomain, "", DEFAULT_MAX_AGE, sslOnlyCookie, true);
        builder.cookie(tokenCookie);

        token.refreshToken().ifPresent(refresh -> {
            var refreshTokenCookie = new NewCookie(REFRESH_TOKEN_COOKIE_NAME, refresh, cookiePath, cookieDomain, "",
                DEFAULT_MAX_AGE, sslOnlyCookie, true);
            builder.cookie(refreshTokenCookie);
        });

        builder.cacheControl(noCache());
        return builder.build();
    }

    private boolean matcherAzureDomain(HttpServletRequest httpServletRequest) {
        var domain = AzureConfigProperties.getAzureDomain();
        return domain != null && Optional.ofNullable(httpServletRequest).map(r -> r.getRequestURL().toString()).filter(u -> u.contains(domain)).isPresent();
    }

    private void cleanCookieJar(Response.ResponseBuilder builder, HttpHeaders headers) {
        String cookieDomain = ServerInfo.instance().getCookieDomain();
        String cookiePath = ServerInfo.instance().getCookiePath();

        /* rydd vekk alle state cookies lagt på. */
        headers.getCookies().entrySet().stream()
            .filter(e -> e.getKey().matches("^state_.+$")
                && Objects.equals(cookieDomain, e.getValue().getDomain())
                && Objects.equals(cookiePath, e.getValue().getPath()))
            .forEach(e -> wipeStateCookie(builder, e.getKey(), e.getValue().getPath(), e.getValue().getDomain()));

    }

    private void wipeStateCookie(Response.ResponseBuilder builder, String cookieName, String cookiePath, String cookieDomain) {
        boolean sslOnlyCookie = ServerInfo.instance().isUsingTLS();
        var deleteCookie = new NewCookie(cookieName, "", cookiePath, cookieDomain, "", 0, sslOnlyCookie, true);
        builder.cookie(deleteCookie);
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

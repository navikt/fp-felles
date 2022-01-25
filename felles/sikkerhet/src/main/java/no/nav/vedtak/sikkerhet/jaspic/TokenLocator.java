package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

class TokenLocator {

    public Optional<OidcTokenHolder> getToken(HttpServletRequest request) {
        Optional<String> tokenFromCookie = getCookie(request, ID_TOKEN_COOKIE_NAME);
        return tokenFromCookie
            .map(s -> new OidcTokenHolder(s, true))
            .or(() -> getTokenFromHeader(request));
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private Optional<String> getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        var cookiePath = ContextPathHolder.instance().getCookiePath();
        return Arrays.stream(request.getCookies())
            .filter(c -> c.getValue() != null)
            .filter(c -> cookieName.equalsIgnoreCase(c.getName()))
            .filter(c -> cookiePath.equalsIgnoreCase(c.getPath()))
            .findFirst()
            .or(() -> Arrays.stream(request.getCookies())
                .filter(c -> c.getValue() != null)
                .filter(c -> cookieName.equalsIgnoreCase(c.getName()))
                .findFirst())
            .map(Cookie::getValue);
    }

    private Optional<OidcTokenHolder> getTokenFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader("Authorization");
        return headerValue != null && headerValue.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE)
                ? Optional.of(new OidcTokenHolder(headerValue.substring(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE.length()), false))
                : Optional.empty();
    }

}

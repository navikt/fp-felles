package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

class TokenLocator {

    public boolean isTokenFromCookie(HttpServletRequest request) {
        return getCookie(request, ID_TOKEN_COOKIE_NAME).isPresent();
    }

    public Optional<TokenString> getToken(HttpServletRequest request) {
        var tokenFromCookie = getCookie(request, ID_TOKEN_COOKIE_NAME);
        return tokenFromCookie
            .or(() -> getTokenFromHeader(request));
    }

    public Optional<TokenString> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private Optional<TokenString> getCookie(HttpServletRequest request, String cookieName) {
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
            .map(Cookie::getValue)
            .map(TokenString::new);
    }

    private Optional<TokenString> getTokenFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader("Authorization");
        return headerValue != null && headerValue.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE)
                ? Optional.of(new TokenString(headerValue.substring(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE.length())))
                : Optional.empty();
    }

}

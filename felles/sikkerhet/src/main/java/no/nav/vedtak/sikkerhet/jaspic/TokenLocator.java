package no.nav.vedtak.sikkerhet.jaspic;

import no.nav.vedtak.sikkerhet.ContextPathHolder;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Optional;

class TokenLocator {

    static final String ID_TOKEN_COOKIE_NAME = "ID_token";

    public Optional<TokenString> getToken(HttpServletRequest request) {
        return getTokenFromHeader(request).or(() -> getCookie(request, ID_TOKEN_COOKIE_NAME));
    }

    private Optional<TokenString> getCookie(HttpServletRequest request, String cookieName) {
        if (!ContextPathHolder.instance().harSattCookiePath() || request.getCookies() == null) {
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
        return headerValue != null && headerValue.startsWith(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE) ? Optional.of(
            new TokenString(headerValue.substring(OpenIDToken.OIDC_DEFAULT_TOKEN_TYPE.length()))) : Optional.empty();
    }

}

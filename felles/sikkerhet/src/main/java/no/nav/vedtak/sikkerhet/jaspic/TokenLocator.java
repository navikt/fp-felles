package no.nav.vedtak.sikkerhet.jaspic;

import static no.nav.vedtak.sikkerhet.Constants.ID_TOKEN_COOKIE_NAME;
import static no.nav.vedtak.sikkerhet.Constants.REFRESH_TOKEN_COOKIE_NAME;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

class TokenLocator {

    public Optional<OidcTokenHolder> getToken(HttpServletRequest request) {
        Optional<String> tokenFromCookie = getCookie(request, ID_TOKEN_COOKIE_NAME);
        if (tokenFromCookie.isPresent()) {
            return Optional.of(new OidcTokenHolder(tokenFromCookie.get(), true));
        }
        return getTokenFromHeader(request);
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        return getCookie(request, REFRESH_TOKEN_COOKIE_NAME);
    }

    private Optional<String> getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (var c : request.getCookies()) {
            if (c.getName().equals(cookieName) && c.getValue() != null) {
                return Optional.of(c.getValue());
            }
        }
        return Optional.empty();
    }

    private Optional<OidcTokenHolder> getTokenFromHeader(HttpServletRequest request) {
        String headerValue = request.getHeader("Authorization");
        return headerValue != null && !headerValue.isEmpty() && headerValue.startsWith("Bearer ")
                ? Optional.of(new OidcTokenHolder(headerValue.substring("Bearer ".length()), false))
                : Optional.empty();
    }

}

package no.nav.vedtak.isso.oidc;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.impl.OpenAmBrukerTokenKlient;

public final class OpenAmTokenProvider {

    private static final Environment ENV = Environment.current();

    private static final String REFRESH_TIME = "no.nav.vedtak.sikkerhet.minimum_time_to_expiry_before_refresh.seconds";
    public static final int DEFAULT_REFRESH_TIME = 120;


    public OpenIDToken exhangeOpenAmAuthCode(String authorizationCode, String callback) {
        return OpenAmBrukerTokenKlient.exhangeAuthCode(authorizationCode, callback);
    }

    public Optional<OpenIDToken> refreshOpenAmIdToken(OpenIDToken expiredToken) {
        return OpenAmBrukerTokenKlient.refreshIdToken(expiredToken, JwtUtil.getClientName(expiredToken.token()));
    }

    public boolean isOpenAmTokenSoonExpired(OpenIDToken token) {
        return tokenIsSoonExpired(token) && token.refreshToken().isPresent();
    }

    private boolean tokenIsSoonExpired(OpenIDToken token) {
        return token.expiresAtMillis() - System.currentTimeMillis() < getMinimumTimeToExpiryBeforeRefresh();
    }

    public static int getMinimumTimeToExpiryBeforeRefresh() {
        return ENV.getProperty(REFRESH_TIME, Integer.class, DEFAULT_REFRESH_TIME) * 1000;
    }
}

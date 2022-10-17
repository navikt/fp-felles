package no.nav.vedtak.isso.oidc;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.impl.OpenAmBrukerTokenKlient;

public final class OpenAmTokenProvider {

    public static final int DEFAULT_REFRESH_TIME = 120;

    private static final String REFRESH_TIME = "no.nav.vedtak.sikkerhet.minimum_time_to_expiry_before_refresh.seconds";
    private static final int MIN_TIME_TO_EXP_BEFORE_REFRESH = Environment.current()
        .getProperty(REFRESH_TIME, Integer.class, DEFAULT_REFRESH_TIME) * 1000;

    public OpenIDToken exhangeOpenAmAuthCode(String authorizationCode, String callback) {
        return OpenAmBrukerTokenKlient.exhangeAuthCode(authorizationCode, callback);
    }

    public Optional<OpenIDToken> refreshOpenAmIdToken(OpenIDToken expiredToken, String clientName) {
        return OpenAmBrukerTokenKlient.refreshIdToken(expiredToken, clientName);
    }

    public boolean isOpenAmTokenSoonExpired(OpenIDToken token) {
        return tokenIsSoonExpired(token) && token.refreshToken().isPresent();
    }

    private boolean tokenIsSoonExpired(OpenIDToken token) {
        return token.expiresAtMillis() - System.currentTimeMillis() < MIN_TIME_TO_EXP_BEFORE_REFRESH;
    }
}

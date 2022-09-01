package no.nav.vedtak.isso.oidc;

import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.isso.ressurs.AzureConfigProperties;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.impl.AzureBrukerTokenKlient;

public final class AzureADTokenProvider {

    private static final Environment ENV = Environment.current();

    private static final String SCOPES = AzureConfigProperties.getAzureScopes().orElse("openid");

    private static final String REFRESH_TIME = "no.nav.vedtak.sikkerhet.minimum_time_to_expiry_before_refresh.seconds";
    public static final int DEFAULT_REFRESH_TIME = 120;


    public static OpenIDToken exhangeAzureAuthCode(String authorizationCode, String callback) {
        return AzureBrukerTokenKlient.exhangeAuthCode(authorizationCode, callback, SCOPES);
    }

    public static Optional<OpenIDToken> refreshAzureIdToken(OpenIDToken expiredToken) {
        return AzureBrukerTokenKlient.refreshIdToken(expiredToken, SCOPES);
    }

    public static boolean isAzureTokenSoonExpired(OpenIDToken token) {
        return tokenIsSoonExpired(token) && token.refreshToken().isPresent();
    }

    private static boolean tokenIsSoonExpired(OpenIDToken token) {
        return token.expiresAtMillis() - System.currentTimeMillis() < getMinimumTimeToExpiryBeforeRefresh();
    }

    public static int getMinimumTimeToExpiryBeforeRefresh() {
        return ENV.getProperty(REFRESH_TIME, Integer.class, DEFAULT_REFRESH_TIME) * 1000;
    }
}

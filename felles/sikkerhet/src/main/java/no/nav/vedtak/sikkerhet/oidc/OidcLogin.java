package no.nav.vedtak.sikkerhet.oidc;

import java.time.Instant;
import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

public class OidcLogin {

    private static final Environment ENV = Environment.current();

    public enum LoginResult {
        SUCCESS,
        ID_TOKEN_MISSING,
        ID_TOKEN_EXPIRED,
        ID_TOKEN_INVALID
    }

    private static final String REFRESH_TIME = "no.nav.vedtak.sikkerhet.minimum_time_to_expiry_before_refresh.seconds";
    public static final int DEFAULT_REFRESH_TIME = 120;

    private final Optional<OidcTokenHolder> idToken;
    private final OidcTokenValidator tokenValidator;

    private String subject;
    private String errorMessage;

    public OidcLogin(Optional<OidcTokenHolder> idToken, OidcTokenValidator tokenValidator) {
        this.idToken = idToken;
        this.tokenValidator = tokenValidator;
    }

    public LoginResult doLogin() {
        if (!idToken.isPresent()) {
            return LoginResult.ID_TOKEN_MISSING;
        }
        OidcTokenValidatorResult validateResult = tokenValidator.validate(idToken.get());
        if (needToRefreshToken(idToken.get(), validateResult)) {
            return LoginResult.ID_TOKEN_EXPIRED;
        }
        if (validateResult.isValid()) {
            this.subject = validateResult.getSubject();
            return LoginResult.SUCCESS;
        }
        errorMessage = validateResult.getErrorMessage();
        return LoginResult.ID_TOKEN_INVALID;
    }

    public String getSubject() {
        return subject;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    private boolean needToRefreshToken(OidcTokenHolder idToken, OidcTokenValidatorResult validateResult) {
        if (validateResult.isValid()) {
            if (idToken.isFromCookie()) {
                return tokenIsSoonExpired(validateResult);
            } else {
                return false;
            }
        }
        return tokenValidator.validateWithoutExpirationTime(idToken).isValid();
    }

    private boolean tokenIsSoonExpired(OidcTokenValidatorResult validateResult) {
        return validateResult.getExpSeconds() * 1000 - Instant.now().toEpochMilli() < getMinimumTimeToExpiryBeforeRefresh();
    }

    public static int getMinimumTimeToExpiryBeforeRefresh() {
        return ENV.getProperty(REFRESH_TIME, Integer.class, DEFAULT_REFRESH_TIME) * 1000;
    }
}

package no.nav.vedtak.sikkerhet.oidc;

public record OidcTokenValidatorResult(boolean isValid, String errorMessage, String subject, long expSeconds)  {

    public static OidcTokenValidatorResult invalid(String errorMessage) {
        return new OidcTokenValidatorResult(false, errorMessage, null, 0);
    }

    public static OidcTokenValidatorResult valid(String subject, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, expSeconds);
    }

    public String getErrorMessage() {
        if (isValid) {
            throw new IllegalArgumentException("Can't get error message from valid token");
        }
        return errorMessage;
    }

    public String getSubject() {
        if (!isValid) {
            throw new IllegalArgumentException("Can't get claims from an invalid token");
        }
        return subject;
    }

    public long getExpSeconds() {
        if (!isValid) {
            throw new IllegalArgumentException("Can't get claims from an invalid token");
        }
        return expSeconds;
    }
}

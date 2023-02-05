package no.nav.vedtak.sikkerhet.oidc.validator;

import no.nav.vedtak.sikkerhet.kontekst.IdentType;

public record OidcTokenValidatorResult(boolean isValid, String errorMessage, String subject, IdentType identType, long expSeconds)  {

    public static OidcTokenValidatorResult invalid(String errorMessage) {
        return new OidcTokenValidatorResult(false, errorMessage, null, null, 0);
    }

    public static OidcTokenValidatorResult valid(String subject, IdentType identType, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, identType, expSeconds);
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

    public IdentType getIdentType() {
        if (!isValid) {
            throw new IllegalArgumentException("Can't get claims from an invalid token");
        }
        return identType;
    }

    public long getExpSeconds() {
        if (!isValid) {
            throw new IllegalArgumentException("Can't get claims from an invalid token");
        }
        return expSeconds;
    }
}

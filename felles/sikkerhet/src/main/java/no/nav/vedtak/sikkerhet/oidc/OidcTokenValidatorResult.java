package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;

public record OidcTokenValidatorResult(boolean isValid, String errorMessage, SluttBruker subject, long expSeconds)  {

    public static OidcTokenValidatorResult invalid(String errorMessage) {
        return new OidcTokenValidatorResult(false, errorMessage, null, 0);
    }

    public static OidcTokenValidatorResult valid(SluttBruker subject, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, expSeconds);
    }

    public String getErrorMessage() {
        if (isValid) {
            throw new IllegalArgumentException("Can't get error message from valid token");
        }
        return errorMessage;
    }

    public SluttBruker getSubject() {
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

package no.nav.vedtak.sikkerhet.oidc.validator;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;

public record OidcTokenValidatorResult(boolean isValid, String errorMessage, String subject, IdentType identType,
                                       String compactSubject, UUID oid, Set<AnsattGruppe> grupper, long expSeconds) {

    private static final String NO_CLAIMS = "Can't get claims from an invalid token";

    public static OidcTokenValidatorResult invalid(String errorMessage) {
        return new OidcTokenValidatorResult(false, errorMessage, null, null, null, null, Set.of(), 0);
    }

    public static OidcTokenValidatorResult valid(String subject, IdentType identType, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, identType, subject, null, Set.of(), expSeconds);
    }

    public static OidcTokenValidatorResult valid(String subject, IdentType identType, UUID oid, Set<AnsattGruppe> grupper, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, identType, subject, oid, grupper, expSeconds);
    }

    public static OidcTokenValidatorResult valid(String subject, IdentType identType, String compactSubject, long expSeconds) {
        return new OidcTokenValidatorResult(true, null, subject, identType, compactSubject, null, Set.of(), expSeconds);
    }



    public String getErrorMessage() {
        if (isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return errorMessage;
    }

    public String getSubject() {
        if (!isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return subject;
    }

    public String getCompactSubject() {
        if (!isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return compactSubject;
    }

    public IdentType getIdentType() {
        if (!isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return identType;
    }

    public Set<AnsattGruppe> getGrupper() {
        if (!isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return grupper;
    }

    public long getExpSeconds() {
        if (!isValid) {
            throw new IllegalArgumentException(NO_CLAIMS);
        }
        return expSeconds;
    }
}

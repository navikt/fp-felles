package no.nav.vedtak.sikkerhet.oidc.token.texas;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IdProvider {
    MASKINPORTEN,
    TOKENX,
    ENTRA_ID,
    IDPORTEN
    ;

    @JsonValue
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}

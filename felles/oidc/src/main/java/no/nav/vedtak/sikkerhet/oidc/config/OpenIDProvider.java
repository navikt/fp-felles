package no.nav.vedtak.sikkerhet.oidc.config;

public enum OpenIDProvider {
    @Deprecated(forRemoval = true) ISSO, // La stå til K9-verdikjede er over.
    STS,
    AZUREAD,
    TOKENX
}

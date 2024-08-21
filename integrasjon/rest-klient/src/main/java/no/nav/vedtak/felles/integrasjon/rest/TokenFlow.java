package no.nav.vedtak.felles.integrasjon.rest;

public enum TokenFlow {
    ADAPTIVE, // DWIM for kall til endepunkt velger azuread eller tokenx ut fra kontekst.
    AZUREAD_CC, // Mot endepunkt som bare støtter AzureCC, ikke AzureOBO-flow
    NO_AUTH_NEEDED; // Enten endepunkt som ikke krever autentisering eller otherAuthorizationSupplier (Maskinporten)

    // Does the endpoint require an Azure AD token?
    public boolean isAzureAD() {
        return AZUREAD_CC.equals(this);
    }

    // Does the endpoint require a system client?
    public boolean isSystemRequired() {
        return AZUREAD_CC.equals(this);
    }
}

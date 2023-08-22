package no.nav.vedtak.felles.integrasjon.rest;

public enum TokenFlow {
    ADAPTIVE, // DWIM for targets accepting both azuread, sts tokens, and tokenx. STS->AzureCC
    STS_CC,
    STS_ADD_CONSUMER, // Trengs inntil videre pga brreg.proxy
    AZUREAD_CC,
    NO_AUTH_NEEDED;

    // Does the endpoint require an Azure AD token?
    public boolean isAzureAD() {
        return AZUREAD_CC.equals(this);
    }

    // Does the endpoint require a system client?
    public boolean isSystemRequired() {
        return STS_CC.equals(this) || AZUREAD_CC.equals(this);
    }
}

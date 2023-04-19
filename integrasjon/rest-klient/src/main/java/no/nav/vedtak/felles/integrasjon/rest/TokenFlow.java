package no.nav.vedtak.felles.integrasjon.rest;

public enum TokenFlow {
    ADAPTIVE, // DWIM for targets accepting both azuread, sts tokens, and tokenx
    ADAPTIVE_ADD_CONSUMER, // ADAPTIVE + adding consumer token
    SYSTEM, // Current system user
    STS_CC,
    STS_ADD_CONSUMER, // Midlertidig til vi har lagt om AAreg til Azure. Skyldes kontextkall i abakus
    AZUREAD_CC,
    NO_AUTH_NEEDED;

    // Does the endpoint require an Azure AD token?
    public boolean isAzureAD() {
        return AZUREAD_CC.equals(this);
    }

    // Does the endpoint require a system client?
    public boolean isSystemRequired() {
        return SYSTEM.equals(this) || STS_CC.equals(this) || AZUREAD_CC.equals(this);
    }
}

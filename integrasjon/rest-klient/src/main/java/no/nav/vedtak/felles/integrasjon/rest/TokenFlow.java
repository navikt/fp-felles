package no.nav.vedtak.felles.integrasjon.rest;

public enum TokenFlow {
    ADAPTIVE, // DWIM for targets accepting both azuread and isso/sts tokens
    ADAPTIVE_ADD_CONSUMER, // DWIM for targets accepting both azuread and isso/sts tokens - adding consumer token
    CONTEXT, // For targets expecting OpenAm/isso or STS tokens. Will pass on token in current security context
    CONTEXT_AZURE, // For targets expecting AzureAD tokens. Will check token+subject in context and perform OBO or CC
    CONTEXT_ADD_CONSUMER, // As CONTEXT but adds consumerToken for system user
    // TOKENX_OBO, not used Requires process of creating assertion, exchanging token, and using the exchanged token
    SYSTEM, // Current system user
    STS_CC,
    AZUREAD_CC
    ;

    // Does the endpoint require an Azure AD token?
    public boolean isAzureAD() {
        return CONTEXT_AZURE.equals(this) || AZUREAD_CC.equals(this);
    }

    // Does the endpoint require a system client?
    public boolean isSystemRequired() {
        return SYSTEM.equals(this) || STS_CC.equals(this) || AZUREAD_CC.equals(this);
    }
}

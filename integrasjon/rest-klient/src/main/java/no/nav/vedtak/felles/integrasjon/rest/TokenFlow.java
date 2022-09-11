package no.nav.vedtak.felles.integrasjon.rest;

public enum TokenFlow {
    CONTEXT, // Pass on the token set for the current Subject (user or system, isso or sts)
    CONTEXT_ADD_CONSUMER, // Pass on the token set for the current Subject and add consumerToken for system user
    // TOKENX_OBO, not used Requires process of creating assertion, exchanging token, and using the exchanged token
    // AZUREAD_OBO, Under development
    SYSTEM, // Current system user
    STS_CC,
    AZUREAD_CC,
}

package no.nav.vedtak.sikkerhet.oidc.config.impl;

/*
 * Interessante elementer fra en standard respons fra .well-known/openid-configuration
 */
public record WellKnownOpenIdConfiguration(String issuer, String jwks_uri, String token_endpoint) {
}

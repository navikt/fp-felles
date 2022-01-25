package no.nav.vedtak.sikkerhet.oidc.config.impl;

/*
 * Interessante elementer fra en standard respons fra .well-known/openid-configuration
 * authorization_endpoint er stort sett interessant for ISSO
 */
public record WellKnownOpenIdConfiguration(String issuer,
                                           String jwks_uri,
                                           String token_endpoint,
                                           String authorization_endpoint) {

}

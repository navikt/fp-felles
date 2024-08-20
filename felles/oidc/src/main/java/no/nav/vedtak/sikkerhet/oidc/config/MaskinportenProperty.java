package no.nav.vedtak.sikkerhet.oidc.config;

/**
 * Standard navn på environment injisert av NAIS når maskinporten er enabled
 * Dvs naiserator:spec:maskinporten:enabled: true
 */
public enum MaskinportenProperty {
    MASKINPORTEN_CLIENT_ID,
    MASKINPORTEN_CLIENT_JWK,
    MASKINPORTEN_SCOPES, // Må angis i naiserator:spec:maskinporten:scopes:consumes: (-name: "<scope>")
    MASKINPORTEN_WELL_KNOWN_URL, // Sanere bruk av well known - bruk heller NAIS/env
    MASKINPORTEN_ISSUER,
    MASKINPORTEN_TOKEN_ENDPOINT

}

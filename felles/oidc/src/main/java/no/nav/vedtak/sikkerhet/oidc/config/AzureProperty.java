package no.nav.vedtak.sikkerhet.oidc.config;

/**
 * Standard navn på environment injisert av NAIS når azure er enabled
 */
public enum AzureProperty {
    AZURE_APP_CLIENT_ID,
    AZURE_APP_CLIENT_SECRET,
    AZURE_APP_JWKS,
    AZURE_APP_JWK,
    AZURE_APP_PRE_AUTHORIZED_APPS,
    AZURE_APP_TENANT_ID,
    AZURE_APP_WELL_KNOWN_URL,
    AZURE_OPENID_CONFIG_ISSUER,
    AZURE_OPENID_CONFIG_JWKS_URI,
    AZURE_OPENID_CONFIG_TOKEN_ENDPOINT
    ;

    public static final String NAV_IDENT = "NAVident";
    public static final String AZP_NAME = "azp_name";

}

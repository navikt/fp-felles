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
    AZURE_APP_WELL_KNOWN_URL;

    public static final String NAV_IDENT = "NAVident";
    public static final String AZP_NAME = "azp_name";
    public static final String GRUPPER = "groups";

}

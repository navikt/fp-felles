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
    //     public  static final String AZURE_WELL_KNOWN_URL = "azure.app.well.known.url"; // naiserator
    //    public  static final String AZURE_CONFIG_ISSUER = "azure.openid.config.issuer"; // naiserator
    //    public  static final String AZURE_CONFIG_JWKS_URI = "azure.openid.config.jwks.uri"; // naiserator
    //    private static final String AZURE_CONFIG_TOKEN_ENDPOINT = "azure.openid.config.token.endpoint"; // naiserator
    //    public  static final String AZURE_CLIENT_ID = "azure.app.client.id"; // naiserator
    //    private static final String AZURE_CLIENT_SECRET = "azure.app.client.secret"; // naiserator

    public static final String NAV_IDENT = "NAVident";
    public static final String AZP_NAME = "azp_name";

}

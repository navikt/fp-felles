package no.nav.vedtak.sikkerhet.pdp.xacml;

/**
 * Inneholder subset av konstanter deklareret i aba-common-attributter modul i
 * Nav.
 *
 * @see abac-common-attributes-alfa / CommonAttributter.
 */
public class NavFellesAttributter {

    public static final String XACML10_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";

    public static final String ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.tokenx_token_body";
    public static final String ENVIRONMENT_FELLES_SAML_TOKEN = "no.nav.abac.attributter.environment.felles.saml_token";
    public static final String ENVIRONMENT_FELLES_OIDC_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.oidc_token_body";

    public static final String ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.azure_jwt_token_body";

    public static final String ENVIRONMENT_FELLES_PEP_ID = "no.nav.abac.attributter.environment.felles.pep_id";

    public static final String RESOURCE_FELLES_RESOURCE_TYPE = "no.nav.abac.attributter.resource.felles.resource_type";
    public static final String RESOURCE_FELLES_DOMENE = "no.nav.abac.attributter.resource.felles.domene";
    public static final String RESOURCE_FELLES_PERSON_FNR = "no.nav.abac.attributter.resource.felles.person.fnr";
    public static final String RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE = "no.nav.abac.attributter.resource.felles.person.aktoerId_resource";

}

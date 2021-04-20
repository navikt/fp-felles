package no.nav.foreldrepenger.sikkerhet.abac.domene;

/**
 * Inneholder subset av konstanter deklareret i abac-common-attributter modul i Nav. Denne ligger ikke tilgjengelig
 * på GPR eller Maven Central, derfor har vi valgt å kopiere de konstanter her.
 */
public class AbacAttributtNøkkel {

    public static final String ACTION_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
    public static final String ENVIRONMENT_OIDC_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.oidc_token_body";
    public static final String ENVIRONMENT_TOKENX_TOKEN_BODY = "no.nav.abac.attributter.environment.felles.tokenx_token_body";
    public static final String ENVIRONMENT_SAML_TOKEN = "no.nav.abac.attributter.environment.felles.saml_token";
    public static final String ENVIRONMENT_PEP_ID = "no.nav.abac.attributter.environment.felles.pep_id";
    public static final String RESOURCE_RESOURCE_TYPE = "no.nav.abac.attributter.resource.felles.resource_type";
    public static final String RESOURCE_DOMENE = "no.nav.abac.attributter.resource.felles.domene";
    public static final String RESOURCE_PERSON_NAVN = "no.nav.abac.attributter.resource.felles.person.navn";
    public static final String RESOURCE_PERSON_FNR = "no.nav.abac.attributter.resource.felles.person.fnr";
    public static final String RESOURCE_PERSON_AKTOERID = "no.nav.abac.attributter.resource.felles.person.aktoerId_resource";
    public static final String SUBJECT_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
    public static final String SUBJECT_TYPE = "no.nav.abac.attributter.subject.felles.subjectType";
    public static final String SUBJECT_LEVEL = "no.nav.abac.attributter.subject.felles.authenticationLevel";

    public static final String RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE = "no.nav.abac.attributter.resource.foreldrepenger.sak.aksjonspunkt_type";
    public static final String RESOURCE_FORELDREPENGER_SAK_ANSVARLIG_SAKSBEHANDLER = "no.nav.abac.attributter.resource.foreldrepenger.sak.ansvarlig_saksbehandler";
    public static final String RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS = "no.nav.abac.attributter.resource.foreldrepenger.sak.behandlingsstatus";
    public static final String RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS = "no.nav.abac.attributter.resource.foreldrepenger.sak.saksstatus";

    public static final String RESOURCE_FORELDREPENGER_ALENEOMSORG = "no.nav.abac.attributter.resource.foreldrepenger.aleneomsorg";
    public static final String RESOURCE_FORELDREPENGER_ANNEN_PART = "no.nav.abac.attributter.resource.foreldrepenger.annen_part";
}

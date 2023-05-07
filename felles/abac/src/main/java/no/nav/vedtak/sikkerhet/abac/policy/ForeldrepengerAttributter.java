package no.nav.vedtak.sikkerhet.abac.policy;

/**
 * Inneholder konstanter bruk i abac-foreldrepenger policies
 */
public class ForeldrepengerAttributter {

    private ForeldrepengerAttributter() {
        // Hindre instans
    }

    /**
     * Attributter brukt i policies
     */
    // Saksrelatert
    public static final String RESOURCE_FP_AKSJONSPUNKT_TYPE = "no.nav.abac.attributter.resource.foreldrepenger.sak.aksjonspunkt_type";
    public static final String RESOURCE_FP_SAKSBEHANDLER = "no.nav.abac.attributter.resource.foreldrepenger.sak.ansvarlig_saksbehandler";
    public static final String RESOURCE_FP_BEHANDLING_STATUS = "no.nav.abac.attributter.resource.foreldrepenger.sak.behandlingsstatus";
    public static final String RESOURCE_FP_SAK_STATUS = "no.nav.abac.attributter.resource.foreldrepenger.sak.saksstatus";
    // Selvbetjeningsrelatert
    public static final String RESOURCE_FP_ALENEOMSORG = "no.nav.abac.attributter.resource.foreldrepenger.aleneomsorg";
    public static final String RESOURCE_FP_ANNEN_PART = "no.nav.abac.attributter.resource.foreldrepenger.annen_part";
    // Ikke tatt i bruk - mangler attributefinder + tilsv for behandlingUuid
    public static final String RESOURCE_FP_SAKSID = "no.nav.abac.attributter.foreldrepenger.fp_saksid";

    /**
     * Verdier brukt i policies
     */
    public static final String VALUE_FP_SAK_STATUS_OPPRETTET = "Opprettet";
    public static final String VALUE_FP_SAK_STATUS_BEHANDLES = "Under behandling";
    public static final String VALUE_FP_BEHANDLING_STATUS_OPPRETTET = "Opprettet";
    public static final String VALUE_FP_BEHANDLING_STATUS_UTREDES = "Behandling utredes";
    public static final String VALUE_FP_BEHANDLING_STATUS_VEDTAK = "Kontroller og fatte vedtak";
    public static final String VALUE_FP_AKSJONSPUNKT_OVERSTYRING = "Overstyring";

    /**
     * Attributter brukt som resource_type
     */
    public static final String RESOURCE_TYPE_FP_APPLIKASJON = "no.nav.abac.attributter.foreldrepenger";
    public static final String RESOURCE_TYPE_FP_DRIFT = "no.nav.abac.attributter.foreldrepenger.drift";
    public static final String RESOURCE_TYPE_FP_FAGSAK = "no.nav.abac.attributter.foreldrepenger.fagsak";
    public static final String RESOURCE_TYPE_FP_VENTEFRIST = "no.nav.abac.attributter.foreldrepenger.fagsak.ventefrist";
    public static final String RESOURCE_TYPE_FP_AVDELINGENHET = "no.nav.abac.attributter.foreldrepenger.oppgavestyring.avdelingsenhet";
    public static final String RESOURCE_TYPE_FP_OPPGAVEKØ = "no.nav.abac.attributter.foreldrepenger.oppgaveko";
    public static final String RESOURCE_TYPE_FP_OPPGAVESTYRING = "no.nav.abac.attributter.foreldrepenger.oppgavestyring";
    public static final String RESOURCE_TYPE_FP_RISIKOKLASSIFISERING = "no.nav.abac.attributter.foreldrepenger.risikoklassifisering";
    public static final String RESOURCE_TYPE_FP_SAKLISTE = "no.nav.abac.attributter.foreldrepenger.sakliste";
    public static final String RESOURCE_TYPE_FP_UTTAKSPLAN = "no.nav.abac.attributter.resource.foreldrepenger.uttaksplan";

    /**
     * Attributter brukt til interne formål
     */
    public static final String RESOURCE_TYPE_INTERNAL_PIP = "pip.tjeneste.kan.kun.kalles.av.pdp.servicebruker";
    public static final String RESOURCE_TYPE_INTERNAL_DUMMY = "";

}

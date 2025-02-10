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
    public static final String RESOURCE_FP_ALENEOMSORG = "no.nav.abac.attributter.resource.foreldrepenger.aleneomsorg"; // Boolean
    // TODO - legg til annen_part_fnr etter sanering abac - forenklet kontroll mot tokenx
    public static final String RESOURCE_FP_ANNEN_PART = "no.nav.abac.attributter.resource.foreldrepenger.annen_part";
    // LOS-relatert - merk samme path som resource ...
    public static final String RESOURCE_FP_AVDELING_ENHET = "no.nav.abac.attributter.foreldrepenger.oppgavestyring.avdelingsenhet";
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
    public static final String VALUE_FP_AVDELING_ENHET_ADRESSEBESKYTTET = "2103";
    public static final String VALUE_FP_AVDELING_ENHET_SKJERMET = "4883";

}

package no.nav.vedtak.log.sporingslogg;

/**
 * Inneholder standard sporingslogg id'er.
 * <p>
 * Brukes det kun i <i>din</i> applikasjon - så implementer heller SporingsloggId interface selv.
 */
public enum StandardSporingsloggId implements SporingsloggId {

    GSAK_SAKSNUMMER("saksnummer"),
    FNR("fnr"),
    FNR_SOK("fnrSok"),
    AKTOR_ID("aktorId"),
    /**
     * @deprecated bruk {@link #BEHANDLING_UUID}
     */
    @Deprecated
    BEHANDLING_ID("behandlingId"),
    BEHANDLING_UUID("behandlingUuid"),

    AKSJONSPUNKT_ID("aksjonspunktId"),
    
    AKSJONSPUNKT_KODE("aksjonspunktKode"),

    JOURNALPOST_ID("journalpostId"),
    DOKUMENT_ID("dokumentId"),
    DOKUMENT_DATA_ID("dokumentDataId"),

    ABAC_DECISION("decision"),
    ABAC_ACTION("abac_action"),
    ABAC_RESOURCE_TYPE("abac_resource_type"),
    ABAC_ANSVALIG_SAKSBEHANDLER("ansvarlig_saksbehandler"),
    ABAC_BEHANDLING_STATUS("behandling_status"),
    ABAC_SAK_STATUS("sak_status"),
    ABAC_AKSJONSPUNKT_TYPE("aksjonspunkt_type"),
    ;

    private String eksternKode;

    StandardSporingsloggId(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    @Override
    public String getSporingsloggKode() {
        return eksternKode;
    }
}

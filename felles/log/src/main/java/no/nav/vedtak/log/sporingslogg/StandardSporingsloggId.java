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
    /**
     * @deprecated Bruk saksnummer
     */
    @Deprecated
    ENHET_ID("enhetId"),

    /**
     * @deprecated Bruk saksnummer
     */
    @Deprecated
    FAGSAK_ID("fagsakId"),

    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    OPPGAVE_ID("oppgaveId"),
    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    BATCH_NAME("batchname"),
    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    BATCH_PARAMETER_NAME("parameter_name"),
    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    BATCH_PARAMETER_VALUE("parameter_value"),
    /**
     * @deprecated Implementer selv i formidling
     */
    @Deprecated
    BREV_MALKODE("brev.malkode"),
    /**
     * @deprecated Implementer selv i formidling
     */
    @Deprecated
    BREV_MOTTAKER("brev.mottaker"),
    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    PROSESS_TASK_STATUS("prosesstask.status"),
    /**
     * @deprecated Implementer selv
     */
    @Deprecated
    PROSESS_TASK_KJORETIDSINTERVALL("prosesstask.kjoretidsintervall"),

    /**
     * Brukes kun av SPBeregning applikasjon.
     * 
     * @deprecated fjern herfra
     **/
    @Deprecated
    SPBEREGNING_ID("beregningId"),

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

package no.nav.vedtak.log.sporingslogg;

public enum SporingsloggId {

    GSAK_SAKSNUMMER("saksnummer"),
    FNR("fnr"),
    FNR_SOK("fnrSok"),
    AKTOR_ID("aktorId"),
    BEHANDLING_ID("behandlingId"),
    AKSJONSPUNKT_ID("aksjonspunktId"),
    JOURNALPOST_ID("journalpostId"),
    DOKUMENT_ID("dokumentId"),
    DOKUMENT_DATA_ID("dokumentDataId"),
    ENHET_ID("enhetId"),
    FAGSAK_ID("fagsakId"),
    OPPGAVE_ID("oppgaveId"),
    BATCH_NAME("batchname"),
    BATCH_PARAMETER_NAME("parameter_name"),
    BATCH_PARAMETER_VALUE("parameter_value"),
    BREV_MALKODE("brev.malkode"),
    BREV_MOTTAKER("brev.mottaker"),
    PROSESS_TASK_STATUS("prosesstask.status"),
    PROSESS_TASK_KJORETIDSINTERVALL("prosesstask.kjoretidsintervall"),

    /** Brukes kun av SPBeregning applikasjon. **/
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

    SporingsloggId(String eksternKode) {
        this.eksternKode = eksternKode;
    }

    public String getEksternKode() {
        return eksternKode;
    }
}

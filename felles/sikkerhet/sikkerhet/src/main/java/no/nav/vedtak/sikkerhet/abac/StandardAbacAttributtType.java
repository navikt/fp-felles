package no.nav.vedtak.sikkerhet.abac;

/**
 * Skal kun inneholde STANDARD ABAC attributt typer. Finner du noe nytt og lurt
 * som du kun bruker i din applikasjon - lag din ege AbacAttributtType
 */
public enum StandardAbacAttributtType implements AbacAttributtType {
    /**
     * Fødselsnummer eller D-nummer
     */
    FNR("fnr", true),

    AKTØR_ID("aktorId", true),

    /**
     * GSAK-saknummer
     */
    SAKSNUMMER("saksnummer"),

    BEHANDLING_ID("behandlingId"),

    DOKUMENT_DATA_ID("dokumentDataId"),

    FAGSAK_ID("fagsakId"),

    /**
     * Eksternt refererbar unik UUID for Behandling. Bør brukes mot andre systemer
     * istdf. BEHANDLING_ID.
     */
    BEHANDLING_UUID("behandlingUuid"),

    AKSJONSPUNKT_KODE("aksjonspunktKode"),

    JOURNALPOST_ID("journalpostId");

    private final String sporingsloggEksternKode;
    private final boolean maskerOutput;

    StandardAbacAttributtType() {
        this(null);
    }

    StandardAbacAttributtType(String sporingsloggEksternKode) {
        this(sporingsloggEksternKode, false);
    }

    StandardAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
    }

    @Override
    public String getSporingsloggKode() {
        return sporingsloggEksternKode;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

}

package no.nav.foreldrepenger.sikkerhet.abac.domene;

/**
 * Skal kun inneholde STANDARD ABAC attributt typer. Finner du noe nytt og lurt
 * som du kun bruker i din applikasjon - lag din ege AbacAttributtType
 */
public enum StandardAbacAttributtType implements AbacAttributtType {
    /** Fødselsnummer eller D-nummer */
    FNR(true),
    AKTØR_ID(true),
    /** GSAK-saknummer */
    SAKSNUMMER,
    BEHANDLING_ID,
    DOKUMENT_DATA_ID,
    FAGSAK_ID,
    /** Eksternt refererbar unik UUID for Behandling. Bør brukes mot andre systemer istdf. BEHANDLING_ID. */
    BEHANDLING_UUID,
    AKSJONSPUNKT_KODE,
    JOURNALPOST_ID;

    private final boolean maskerOutput;

    StandardAbacAttributtType() {
        this(false);
    }

    StandardAbacAttributtType(boolean maskerOutput) {
        this.maskerOutput = maskerOutput;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

}

package no.nav.vedtak.sikkerhet.abac;

/**
 * Skal kun inneholde STANDARD ABAC attributt typer. Finner du noe nytt og lurt som du kun bruker i din applikasjon - lag din ege
 * AbacAttributtType
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

    /** Eksternt refererbar unik UUID for Behandling. Bør brukes mot andre systemer istdf. BEHANDLING_ID. */
    BEHANDLING_UUID("behandlingUuid"),
    
    AKSJONSPUNKT_KODE("aksjonspunktKode"),
    
    JOURNALPOST_ID("journalpostId"),
    
    /**
     * Indikerer at alle saker som har dette fødselsnummeret som søker må sjekkes.
     * 
     * @deprecated Implementer selv
     */
    SAKER_MED_FNR(),


    /**
     * Samme som JOURNALPOST_ID, men det skal kreves at denne journalpost_Id finnes
     * 
     * @deprecated Bør bare bruke {@link #JOURNALPOST_ID}
     */
    @Deprecated
    EKSISTERENDE_JOURNALPOST_ID("journalpostId", false, true),


    /**
     * @deprecated Implementer selv i fordel
     */
    @Deprecated
    OPPGAVE_ID("oppgaveId"),

    /**
     * @deprecated Implementer selv i fpsak
     */
    @Deprecated
    /* vi kan ikke bruke DokumentID til tilgangskontroll, men har den med for å populere sporingslogg */
    DOKUMENT_ID("dokumentId"),

    /**
     * Skal kun brukes i spberegning. Brukes ikke til tilgangskontroll, men for sporing.
     * 
     * @deprecated - flytt til spberegning
     */
    @Deprecated
    SPBEREGNING_ID,

    /**
     * @deprecated Implementer selv i fordel
     */
    @Deprecated
    DOKUMENTFORSENDELSE_ID,

    /**
     * @deprecated Flytt til LOS
     */
    @Deprecated
    OPPGAVESTYRING_ENHET;

    private final String sporingsloggEksternKode;
    private final boolean maskerOutput;
    private final boolean valider;

    StandardAbacAttributtType() {
        sporingsloggEksternKode = null;
        maskerOutput = false;
        valider = false;
    }

    StandardAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
        valider = false;
    }

    StandardAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
        valider = false;
    }

    StandardAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput, boolean valider) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
        this.valider = valider;
    }

    @Override
    public String getSporingsloggEksternKode() {
        return sporingsloggEksternKode;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

    @Override
    public boolean getValider() {
        return valider;
    }
}

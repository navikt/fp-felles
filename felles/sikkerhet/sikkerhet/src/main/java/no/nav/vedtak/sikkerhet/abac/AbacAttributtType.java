package no.nav.vedtak.sikkerhet.abac;

public enum AbacAttributtType {
    /**
     * Fødselsnummer eller D-nummer
     */
    FNR,

    AKTØR_ID,

    /**
     * GSAK-saknummer
     */
    SAKSNUMMER,

    BEHANDLING_ID,
    DOKUMENT_DATA_ID,
    FAGSAK_ID,

    /** Eksternt refererbar unik UUID for Behandling. Bør brukes mot andre systemer istdf. BEHANDLING_ID. */
    BEHANDLING_UUID,
    /**
     * Indikerer at alle saker som har dette fødselsnummeret som søker må sjekkes.
     */
    SAKER_MED_FNR,

    JOURNALPOST_ID,

    /**
     * Samme som JOURNALPOST_ID, men det skal kreves at denne journalpost_Id finnes
     */
    EKSISTERENDE_JOURNALPOST_ID,

    AKSJONSPUNKT_KODE,

    OPPGAVE_ID,

    /* vi kan ikke bruke DokumentID til tilgangskontroll, men har den med for å populere sporingslogg */
    DOKUMENT_ID,
    
    /**
     * Skal kun brukes i spberegning. Brukes ikke til tilgangskontroll, men for sporing.
     */
    SPBEREGNING_ID,

    DOKUMENTFORSENDELSE_ID,
    
    OPPGAVESTYRING_ENHET
}

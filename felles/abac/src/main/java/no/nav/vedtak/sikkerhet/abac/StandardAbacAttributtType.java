package no.nav.vedtak.sikkerhet.abac;

import java.util.Set;

/**
 * Skal kun inneholde STANDARD ABAC attributt typer. Finner du noe nytt og lurt
 * som du kun bruker i din applikasjon - lag din ege AbacAttributtType
 */
public enum StandardAbacAttributtType implements AbacAttributtType {
    /**
     * Fødselsnummer eller D-nummer
     */
    FNR,
    AKTØR_ID,

    /**
     * Interne referanser som ikke bør eksponeres utenfor applikasjonen.
     */
    FAGSAK_ID,
    BEHANDLING_ID,
    AKSJONSPUNKT_KODE,

    /**
     * Eksternt anvendbare referanser - saksnummer, unik UUID for Behandling.
     */
    SAKSNUMMER,
    BEHANDLING_UUID,
    JOURNALPOST_ID;

    private static final Set<StandardAbacAttributtType> MASKERES = Set.of(FNR, AKTØR_ID);

    @Override
    public boolean getMaskerOutput() {
        return MASKERES.contains(this);
    }

}

package no.nav.vedtak.felles.integrasjon.behandleoppgave;


/**
 * @deprecated erstattes av OppgaveÅrsak {@link OppgaveÅrsak}.
 */
@Deprecated
public enum OppgaveKodeType {

    JFR_FOR("Journalføring foreldrepenger - JFR_FOR er ikke dokumentert i tjenestedokumentasjon, men er koden som blir brukt i kodeverk i GSAK"),
    FDR_FOR("Fordeling foreldrepenger - FDR_FOR skal brukes for fordelingsoppgaver.");

    @SuppressWarnings("unused")
    private String description;

    OppgaveKodeType(String description) {
        this.description = description;
    }

    public static OppgaveKodeType fraString(String kode) {
        for (OppgaveKodeType key : values()) {
            if (key.toString().equals(kode)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Finner ikke oppgavekode med key: " + kode);
    }
}

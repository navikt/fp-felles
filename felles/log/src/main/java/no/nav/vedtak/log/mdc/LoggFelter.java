package no.nav.vedtak.log.mdc;

/**
 * Valige felt i logginnslag. Vil hete "x_prosess_<felt>" i logginnslag.
 */
public final class LoggFelter {

    // Standard felter
    public static final String SAK = "fagsak";
    public static final String BEHANDLING = "behandling"; // UUID
    public static final String STEG = "steg";

    // Vanlige felt
    public static final String BEHANDLING_ID = "behandlingId";
    public static final String JOURNALPOST_ID = "journalpostId";

    private LoggFelter() {
    }

}

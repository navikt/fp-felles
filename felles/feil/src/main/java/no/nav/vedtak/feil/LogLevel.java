package no.nav.vedtak.feil;

@Deprecated
/**
 *
 * @deprecated Denne wrappingen gir lite ekstra verdi, bruk slf4j
 */
public enum LogLevel {
    ERROR,
    WARN,

    /**
     * Når cause er en {@link no.nav.vedtak.exception.VLException}, arves logLevel
     * fra denne. I motsatt fall behandles denne som WARN.
     * <p>
     * Gjør at {@link no.nav.vedtak.exception.VLException} kan wrappe andre
     * {@link no.nav.vedtak.exception.VLException} og fortsatt beholde logLevel fra
     * wrappet exception
     */
    INHERIT,

    INFO;
}

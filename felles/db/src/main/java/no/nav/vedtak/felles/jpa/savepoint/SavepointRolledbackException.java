package no.nav.vedtak.felles.jpa.savepoint;

/**
 * Wrapper exception for å kunne kaste en exception etter et savepoint rollback,
 * men som ikke medfører at transaksjonen rulles tilbake (men commiter
 * restrerende).
 */
public class SavepointRolledbackException extends RuntimeException {

    public SavepointRolledbackException(Throwable cause) {
        this(null, cause);
    }

    public SavepointRolledbackException(String message, Throwable cause) {
        super(message, cause);
    }
}

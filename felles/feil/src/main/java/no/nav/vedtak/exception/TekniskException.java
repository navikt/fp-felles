package no.nav.vedtak.exception;
@Deprecated(since = "3.1", forRemoval = true)
/* Bruk klasser fra no.nav.foreldrepenger.felles:feil:1.0.1 istedenfor. */
public class TekniskException extends VLException {

    public TekniskException(String kode, String msg) {
        this(kode, msg, null);
    }

    public TekniskException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}

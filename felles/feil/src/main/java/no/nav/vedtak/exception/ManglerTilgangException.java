package no.nav.vedtak.exception;
/* Bruk klasser fra no.nav.foreldrepenger.felles:feil:1.0.1 istedenfor. */
public class ManglerTilgangException extends VLException {

    public ManglerTilgangException(String kode, String msg) {
        this(kode, msg, null);
    }

    public ManglerTilgangException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}

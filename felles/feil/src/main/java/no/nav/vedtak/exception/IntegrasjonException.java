package no.nav.vedtak.exception;
/* Bruk klasser fra no.nav.foreldrepenger.felles:feil:1.0.1 istedenfor. */
public class IntegrasjonException extends VLException {

    public IntegrasjonException(String kode, String msg) {
        this(kode, msg, null);
    }

    public IntegrasjonException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}

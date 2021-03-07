package no.nav.vedtak.exception;

public class IntegrasjonException extends VLException {

    public IntegrasjonException(String kode, String msg) {
        this(kode, msg, null);
    }

    public IntegrasjonException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}

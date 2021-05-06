package no.nav.vedtak.exception;

public class TekniskException extends VLException {

    public TekniskException(String kode, String msg) {
        this(kode, msg, null);
    }

    public TekniskException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }
}

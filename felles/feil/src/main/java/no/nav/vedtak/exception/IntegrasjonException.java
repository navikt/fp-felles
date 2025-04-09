package no.nav.vedtak.exception;

public class IntegrasjonException extends VLException {

    private final Integer statusCode;

    public IntegrasjonException(String kode, String msg) {
        this(kode, msg, null, null);
    }

    public IntegrasjonException(String kode, String msg, Throwable cause) {
        this(kode, msg, null, cause);
    }

    public IntegrasjonException(String kode, String msg, int statusCode) {
        this(kode, msg, statusCode, null);
    }

    public IntegrasjonException(String kode, String msg, Integer statusCode, Throwable cause) {
        super(kode, msg, cause);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}

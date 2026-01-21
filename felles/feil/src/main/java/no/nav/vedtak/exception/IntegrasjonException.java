package no.nav.vedtak.exception;

public class IntegrasjonException extends VLException {

    private final Integer statusCode;
    private final String feil;

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
        this.feil = null;
    }

    public IntegrasjonException(String kode, String msg, Integer statusCode, String feil, Throwable cause) {
        super(kode, formaterFeilmelding(msg, feil), cause);
        this.statusCode = statusCode;
        this.feil = feil;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getFeil() {
        return feil;
    }

    private static String formaterFeilmelding(String msg, String feil) {
        return feil != null ? "%s. Detaljert feilmedling: %s".formatted(msg, feil) : msg;
    }
}

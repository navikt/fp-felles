package no.nav.vedtak.exception;

import java.net.HttpURLConnection;

public non-sealed class IntegrasjonException extends VLException {

    // Inneholder feilobjekt. Ved kall til FP-apps kanskje en FeilDto som kan propageres
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
        this(kode, msg, statusCode, null, cause);
    }

    public IntegrasjonException(String kode, String msg, Integer statusCode, String feil, Throwable cause) {
        super(kode, formaterFeilmelding(msg, feil), cause);
        this.statusCode = statusCode;
        this.feil = feil;
    }

    public String getFeil() {
        return feil;
    }

    private static String formaterFeilmelding(String msg, String feil) {
        return feil != null ? "%s. Detaljert feilmedling: %s".formatted(msg, feil) : msg;
    }

    @Override
    public int getStatusCode() {
        return statusCode != null ? statusCode : HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}

package no.nav.vedtak.exception;

import java.net.HttpURLConnection;

public non-sealed class TekniskException extends VLException {

    public TekniskException(String kode, String msg) {
        this(kode, msg, null);
    }

    public TekniskException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}

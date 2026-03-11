package no.nav.vedtak.exception;

import java.net.HttpURLConnection;

public non-sealed class FunksjonellException extends VLException {

    private final String løsningsforslag;

    public FunksjonellException(String kode, String msg) {
        this(kode, msg, null);
    }

    public FunksjonellException(String kode, String msg, String hint) {
        this(kode, msg, hint, null);
    }

    public FunksjonellException(String kode, String msg, String løsningsforslag, Throwable t) {
        super(kode, formaterFeilmelding(msg, løsningsforslag), t);
        this.løsningsforslag = løsningsforslag;
    }

    private static String formaterFeilmelding(String msg, String løsningsforslag) {
        return løsningsforslag != null ? "%s. %s".formatted(msg, løsningsforslag) : msg;
    }

    public String getLøsningsforslag() {
        return løsningsforslag;
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_BAD_REQUEST;
    }

}

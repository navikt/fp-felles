package no.nav.vedtak.exception;

import java.net.HttpURLConnection;

import no.nav.vedtak.feil.Feilkode;

public non-sealed class ManglerTilgangException extends VLException {

    public ManglerTilgangException(String kode, String msg) {
        this(kode, msg, null);
    }

    public ManglerTilgangException(String kode, String msg, Throwable cause) {
        super(kode, msg, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_FORBIDDEN;
    }

    @Override
    public String getFeilkode() {
        return Feilkode.IKKE_TILGANG.name();
    }

    @Override
    public VLLogLevel getLogLevel() {
        return VLLogLevel.NOLOG;
    }
}

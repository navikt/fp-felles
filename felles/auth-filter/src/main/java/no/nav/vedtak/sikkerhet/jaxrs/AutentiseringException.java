package no.nav.vedtak.sikkerhet.jaxrs;

import java.net.HttpURLConnection;

import no.nav.vedtak.exception.ManglerTilgangException;

public class AutentiseringException extends ManglerTilgangException {

    public AutentiseringException(String msg, Throwable cause) {
        super("Autentiseringsfeil", msg, cause);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_UNAUTHORIZED;
    }
}

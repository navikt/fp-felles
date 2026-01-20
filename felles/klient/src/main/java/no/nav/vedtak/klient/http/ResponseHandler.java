package no.nav.vedtak.klient.http;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Set;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

final class ResponseHandler {

    private ResponseHandler() {
    }

    static <W> W handleResponse(final HttpResponse<W> response, URI endpoint, Set<Integer> acceptStatus) {
        int status = response.statusCode();
        if (status == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        }
        if ((status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE) || acceptStatus.contains(status)) {
            return response.body();
        }
        if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Feilet mot " + endpoint);
        }
        var feilEntitet = (response.body() instanceof String s) ? s : null;
        throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra %s", status, endpoint), status, feilEntitet, null);
    }

    static <W> HttpResponse<W> handleRawResponse(final HttpResponse<W> response, URI endpoint, Set<Integer> acceptStatus) {
        int status = response.statusCode();
        if (status == HttpURLConnection.HTTP_NO_CONTENT) {
            return null;
        }
        if ((status >= HttpURLConnection.HTTP_OK && status < HttpURLConnection.HTTP_MULT_CHOICE) || acceptStatus.contains(status)) {
            return response;
        }
        if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new ManglerTilgangException("F-468816", "Feilet mot " + endpoint);
        }
        var feilEntitet = (response.body() instanceof String s) ? s : null;
        throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra %s", status, endpoint), status, feilEntitet, null);
    }

}

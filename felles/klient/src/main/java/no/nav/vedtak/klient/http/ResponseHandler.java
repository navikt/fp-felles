package no.nav.vedtak.klient.http;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Set;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

final class ResponseHandler {

    private ResponseHandler() {
        // NOSONAR
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
        throw new IntegrasjonException("F-468817", String.format("Uventet respons %s fra %s", status, endpoint));
    }

}

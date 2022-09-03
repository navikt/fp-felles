package no.nav.vedtak.felles.integrasjon.rest;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Set;

import no.nav.vedtak.klient.http.DefaultHttpKlient;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public final class RestKlient {

    private static volatile RestKlient CLIENT; // NOSONAR

    private final DefaultHttpKlient httpClient;

    private RestKlient() {
        this.httpClient = DefaultHttpKlient.client();
    }

    public static synchronized RestKlient client() {
        var inst= CLIENT;
        if (inst == null) {
            inst = new RestKlient();
            CLIENT = inst;
        }
        return inst;
    }

    public <T> T send(HttpRequest request, Class<T> clazz) {
        RestRequest.validateRestHeaders(request);
        var response = httpClient.send(request);
        return DefaultJsonMapper.fromJson(response, clazz);
    }

    public <T> T sendPermitConflict(HttpRequest request, Class<T> clazz) {
        RestRequest.validateRestHeaders(request);
        var response= httpClient.send(request, Set.of(HttpURLConnection.HTTP_CONFLICT));
        return DefaultJsonMapper.fromJson(response, clazz);
    }

    public String send(HttpRequest request) {
        RestRequest.validateRestHeaders(request);
        return httpClient.send(request);
    }

    public String sendPermitConflict(HttpRequest request) {
        RestRequest.validateRestHeaders(request);
        return httpClient.send(request, Set.of(HttpURLConnection.HTTP_CONFLICT));
    }

    public Optional<byte[]> sendHandleResponse(HttpRequest request) {
        RestRequest.validateRestHeaders(request);
        return httpClient.sendHandleResponse(request);
    }

    // For de som vil håndtere statusCode() og body() selv - men husk å kaste exception der det ikke skal ignoreres.
    public HttpResponse<String> sendNoResponseHandler(HttpRequest request) {
        RestRequest.validateRestHeaders(request);
        return httpClient.sendNoResponseHandler(request);
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Set;

import no.nav.vedtak.klient.http.DefaultHttpClient;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public final class RestSender {

    private final DefaultHttpClient httpklient;

    public RestSender() {
        this.httpklient = DefaultHttpClient.client();
    }

    public <T> T send(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return DefaultJsonMapper.fromJson(response, clazz);
    }

    public <T> T sendExpectConflict(RestRequest request, Class<T> clazz) {
        var response= sendExpectConflict(request);
        return DefaultJsonMapper.fromJson(response, clazz);
    }

    public String send(RestRequest request) {
        return httpklient.send(request);
    }

    public String sendExpectConflict(RestRequest request) {
        return httpklient.send(request, Set.of(HttpURLConnection.HTTP_CONFLICT));
    }

    public Optional<byte[]> sendHandleResponse(RestRequest request) {
        return httpklient.sendHandleResponse(request);
    }

    // For de som vil håndtere statusCode() og body() selv - men husk å kaste exception der det ikke skal ignoreres.
    public HttpResponse<String> unhandledResponseSend(RestRequest request) {
        return httpklient.sendNoResponseHandler(request);
    }
}

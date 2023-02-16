package no.nav.vedtak.felles.integrasjon.rest;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import no.nav.vedtak.klient.http.DefaultHttpClient;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

public final class RestClient {

    private final DefaultHttpClient httpklient;

    private RestClient() {
        this.httpklient = DefaultHttpClient.client();
    }

    public static RestClient client() {
        return new RestClient();
    }

    public <T> T send(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return mapResponse(response, s -> false, clazz);
    }

    public <T> T sendExpectConflict(RestRequest request, Class<T> clazz) {
        var response= httpklient.send(request, Set.of(HttpURLConnection.HTTP_CONFLICT));
        return mapResponse(response, s -> false, clazz);
    }

    public <T> Optional<T> sendReturnOptional(RestRequest request, Class<T> clazz) {
        var response= httpklient.send(request);
        return Optional.ofNullable(mapResponse(response, String::isEmpty, clazz));
    }

    public <T> List<T> sendReturnList(RestRequest request, Class<T> clazz) {
        var response= httpklient.send(request);
        return DefaultJsonMapper.listFromJson(response, clazz);
    }

    public <T> Map<String, T> sendReturnMap(RestRequest request, Class<T> clazz) {
        var response= httpklient.send(request);
        return DefaultJsonMapper.mapFromJson(response, clazz);
    }

    public byte[] sendReturnByteArray(RestRequest request) {
        return httpklient.sendReturnByteArray(request);
    }

    /**
     * Raw response, not checked for status codes 4nn or 5nn - please ensure that any usage avoids "quiet errors"
     */
    public HttpResponse<String> sendReturnUnhandled(RestRequest request) {
        return httpklient.sendReturnUnhandled(request);
    }

    public HttpResponse<String> sendReturnUnhandledNoRetry(RestRequest request) {
        return httpklient.sendReturnUnhandledNoRetry(request);
    }

    private <T> T mapResponse(String response, Predicate<String> filterOut, Class<T> clazz) {
        if (response == null || filterOut.test(response)) {
            return null;
        }
        if (clazz.isAssignableFrom(String.class)) {
            return clazz.cast(response);
        }
        return DefaultJsonMapper.fromJson(response, clazz);
    }
}

package no.nav.vedtak.felles.integrasjon.rest;

import no.nav.vedtak.klient.http.BaseHttpClient;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

class BaseRestClient {

    private BaseHttpClient httpklient;

    public BaseRestClient(BaseHttpClient httpClient) {
        this.httpklient = httpClient;
    }

    public <T> T send(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return mapResponse(response, s -> false, clazz);
    }

    public <T> T sendExpectConflict(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request, Set.of(HttpURLConnection.HTTP_CONFLICT));
        return mapResponse(response, s -> false, clazz);
    }

    public <T> Optional<T> sendReturnOptional(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return Optional.ofNullable(mapResponse(response, String::isEmpty, clazz));
    }

    public <T> List<T> sendReturnList(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return DefaultJsonMapper.listFromJson(response, clazz);
    }

    public <T> Map<String, T> sendReturnMap(RestRequest request, Class<T> clazz) {
        var response = httpklient.send(request);
        return DefaultJsonMapper.mapFromJson(response, clazz);
    }

    public byte[] sendReturnByteArray(RestRequest request) {
        return httpklient.sendReturnByteArray(request);
    }

    public HttpResponse<String> sendReturnResponseString(RestRequest request) {
        return httpklient.sendReturnResponse(request);
    }

    public HttpResponse<byte[]> sendReturnResponseByteArray(RestRequest request) {
        return httpklient.sendReturnResponseByteArray(request);
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

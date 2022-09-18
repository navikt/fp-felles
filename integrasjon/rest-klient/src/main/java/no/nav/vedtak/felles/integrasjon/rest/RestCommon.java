package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.net.http.HttpRequest;

/**
 * Utilities for compact creatation of common requests
 */
public final class RestCommon {


    public static RestRequest get(URI target, Class<?> annotated) {
        return RestRequest.newRequest(RestRequest.Method.get(), target, annotated);
    }

    public static RestRequest postJson(Object body, URI target, Class<?> annotated) {
        return RestRequest.newRequest(RestRequest.Method.postJson(body), target, annotated);
    }

    public static RestRequest publishJson(RestRequest.WebMethod method, Object body, URI target, Class<?> annotated) {
        return publish(method, RestRequest.jsonPublisher(body), target, annotated);
    }

    public static RestRequest publish(RestRequest.WebMethod method, HttpRequest.BodyPublisher bodyPublisher, URI target, Class<?> annotated) {
        return RestRequest.newRequest(new RestRequest.Method(method, bodyPublisher), target, annotated);
    }

    public static RestRequest get(URI target, TokenFlow tf, String scopes) {
        return RestRequest.newRequest(RestRequest.Method.get(), target, tf, scopes);
    }

    public static RestRequest postJson(Object body, URI target, TokenFlow tf, String scopes) {
        return  RestRequest.newRequest(RestRequest.Method.postJson(body), target, tf, scopes);
    }

    public static RestRequest publishJson(RestRequest.WebMethod method, Object body, URI target, TokenFlow tf, String scopes) {
        return publish(method, RestRequest.jsonPublisher(body), target, tf, scopes);
    }

    public static RestRequest publish(RestRequest.WebMethod method, HttpRequest.BodyPublisher bodyPublisher, URI target, TokenFlow tf, String scopes) {
        return RestRequest.newRequest(new RestRequest.Method(method, bodyPublisher), target, tf, scopes);
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Optional;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

/**
 * Simplified interface to most common rest calls in current applications
 * - Set of methods requiring RestClientConfig-annotation on caller-class. Will pick token+scopes from annotation
 * - Set of methods with tokenflow and scopes parameters
 * - Legacy, on-prem integrations passing on tokens (issued by isso or sts) .
 *
 * For integrations with specific request-requirements (headers) or response-conventions (409), use RestRequest + RestSender.
 * Examples can be found in modules in felles-integrasjon
 *
 */
public final class RestCompact {

    public enum Method {
        POST, PUT, PATCH
    }

    private final RestSender restClient;

    public RestCompact() {
        this.restClient = new RestSender();
    }

    /**
     * Caller-class parameters, requiring @RestClientConfig (RCC)
     */
    public <T> T getValue(Class<?> caller, URI target, Class<T> mapTo) {
        var response = getString(caller, target);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String getString(Class<?> caller, URI target) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.send(RestRequest.buildFor(caller, builder));
    }

    public Optional<byte[]> getBytes(Class<?> caller, URI target) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.sendHandleResponse(RestRequest.buildFor(caller, builder));
    }

    public <T> T postValue(Class<?> caller, URI target, Object body, Class<T> mapTo) {
        var response = pmethodString(caller, Method.POST, target, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String postString(Class<?> caller, URI target, Object body) {
        return pmethodString(caller, Method.POST, target, body);
    }

    public <T> T pmethodValue(Class<?> caller, Method method, URI target, Object body, Class<T> mapTo) {
        var response = pmethodString(caller, method, target, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String pmethodString(Class<?> caller, Method method, URI target, Object body) {
        var builder = getBuilderFor(method, target, body);
        return restClient.send(RestRequest.buildFor(caller, builder));
    }

    /**
     * Methods specifying target, token/flow, and scopes (only expected for Azure-flows)
     */
    public <T> T getValue(URI target, TokenFlow tf, String scopes, Class<T> mapTo) {
        var response = getString(target, tf, scopes);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String getString(URI target, TokenFlow tf, String scopes) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.send(RestRequest.build(builder, tf, scopes));
    }

    public Optional<byte[]> getBytes(URI target, TokenFlow tf, String scopes) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.sendHandleResponse(RestRequest.build(builder, tf, scopes));
    }

    public <T> T postValue(URI target, TokenFlow tf, String scopes, Object body, Class<T> mapTo) {
        var response = pmethodString(target, tf, scopes, Method.POST, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String postString(URI target, TokenFlow tf, String scopes, Object body) {
        return pmethodString(target, tf, scopes, Method.POST, body);
    }

    public <T> T pmethodValue(URI target, TokenFlow tf, String scopes, Method method, Object body, Class<T> mapTo) {
        var response = pmethodString(target, tf, scopes, method, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String pmethodString(URI target, TokenFlow tf, String scopes, Method method, Object body) {
        var builder = getBuilderFor(method, target, body);
        return restClient.send(RestRequest.build(builder, tf, scopes));
    }

    /**
     * Legacy, on-prem, token-passing of isso/sts-tokens. Requires tokens in current security context
     */
    public <T> T contextGetValue(URI target, Class<T> mapTo) {
        var response = contextGetString(target);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String contextGetString(URI target) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.send(RestRequest.buildFor(builder, TokenFlow.CONTEXT));
    }

    public Optional<byte[]> contextGetBytes(URI target) {
        var builder = HttpRequest.newBuilder(target).GET();
        return restClient.sendHandleResponse(RestRequest.buildFor(builder, TokenFlow.CONTEXT));
    }

    public <T> T contextPostValue(URI target, Object body, Class<T> mapTo) {
        var response = contextPmethodString(Method.POST, target, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String contextPostString(URI target, Object body) {
        return contextPmethodString(Method.POST, target, body);
    }

    public <T> T contextPmethodValue(Method method, URI target, Object body, Class<T> mapTo) {
        var response = contextPmethodString(method, target, body);
        return DefaultJsonMapper.fromJson(response, mapTo);
    }

    public String contextPmethodString(Method method, URI target, Object body) {
        var builder = getBuilderFor(method, target, body);
        return restClient.send(RestRequest.buildFor(builder, TokenFlow.CONTEXT));
    }

    private HttpRequest.Builder getBuilderFor(Method method, URI target, Object body) {
        return switch (method) {
            case PATCH -> HttpRequest.newBuilder(target).method(method.name(), RestRequest.jsonPublisher(body));
            case POST -> HttpRequest.newBuilder(target).POST(RestRequest.jsonPublisher(body));
            case PUT -> HttpRequest.newBuilder(target).PUT(RestRequest.jsonPublisher(body));
        };
    }

}

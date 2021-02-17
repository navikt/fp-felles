package no.nav.vedtak.felles.integrasjon.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.DefaultJsonMapperFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.ByteArrayResponseHandler;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.StringUtils;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og
 * callId
 *
 * @deprecated @see AbstractJerseyOidcRestClient
 */
@Deprecated(since = "3.0.x", forRemoval = true)
public abstract class AbstractOidcRestClient extends CloseableHttpClient {
    private static final String DEFAULT_NAV_CONSUMERID = "Nav-Consumer-Id";
    private static final String DEFAULT_NAV_CALLID = "Nav-Callid";
    public static final String ALT_NAV_CALL_ID = "nav-call-id";

    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";

    private CloseableHttpClient client;

    private ObjectMapper mapper = DefaultJsonMapper.getObjectMapper();

    public AbstractOidcRestClient(CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    /** @deprecated for kompatibilitet med CloseableHttpClient. IKKE BRUK. */
    @Deprecated(forRemoval = true, since = "2.3.x")
    @Override
    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    /** @deprecated for kompatibilitet med CloseableHttpClient. IKKE BRUK. */
    @Deprecated(forRemoval = true, since = "2.3.x")
    @Override
    public HttpParams getParams() {
        return client.getParams();
    }

    public <T> T get(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createStringResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public <T> T get(URI endpoint, Set<Header> headers, Class<T> clazz) {
        String entity = get(endpoint, headers, Set.of(), createStringResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public <T> T get(URI endpoint, Set<Header> headers, Set<String> extraAuthHeaders, Class<T> clazz) {
        String entity = get(endpoint, headers, extraAuthHeaders, createStringResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public String get(URI endpoint) {
        return get(endpoint, createStringResponseHandler(endpoint));
    }

    public <T> Optional<T> getReturnsOptional(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createStringResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(fromJson(entity, clazz));
    }

    public String patch(URI endpoint, Object dto) {
        return patch(endpoint, dto, Collections.emptySet(), createStringResponseHandler(endpoint));
    }

    public String patch(URI endpoint, Object dto, Set<Header> headers) {
        return patch(endpoint, dto, headers, createStringResponseHandler(endpoint));
    }

    public String post(URI endpoint, Object dto) {
        return post(endpoint, dto, createStringResponseHandler(endpoint));
    }

    public <T> T post(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createStringResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public <T> T post(URI endpoint, Object dto, Set<Header> headers, Class<T> clazz) {
        String entity = post(endpoint, dto, headers, createStringResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public <T> Optional<T> postReturnsOptional(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createStringResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(fromJson(entity, clazz));
    }

    public Optional<byte[]> postReturnsOptionalOfByteArray(URI endpoint, Object dto) {
        byte[] entity = post(endpoint, dto, createByteArrayResponseHandler(endpoint));
        return Optional.ofNullable(entity);
    }

    public String put(URI endpoint, Object dto) {
        return put(endpoint, dto, Collections.emptySet(), createStringResponseHandler(endpoint));
    }

    public String put(URI endpoint, Object dto, Set<Header> headers) {
        return put(endpoint, dto, headers, createStringResponseHandler(endpoint));
    }

    protected ResponseHandler<String> createStringResponseHandler(URI endpoint) {
        return new StringResponseHandler(endpoint);
    }

    protected ResponseHandler<byte[]> createByteArrayResponseHandler(URI endpoint) {
        return new ByteArrayResponseHandler(endpoint);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader(ACCEPT, APPLICATION_JSON.getMimeType());
        request.setHeader(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + getOIDCToken());
        request.setHeader(DEFAULT_NAV_CALLID, MDCOperations.getCallId());
        request.setHeader(DEFAULT_NAV_CONSUMERID, getConsumerId());
        request.setHeader(ALT_NAV_CALL_ID, MDCOperations.getCallId());
        return client.execute(target, request, context);
    }

    protected String getConsumerId() {
        return SubjectHandler.getSubjectHandler().getConsumerId();
    }

    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw DefaultJsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }

    /** Få tak i en kopi av definert ObjectMapper. */
    public ObjectMapper getMapper() {
        return mapper.copy();
    }

    protected String get(URI endpoint, ResponseHandler<String> responseHandler) {
        HttpGet get = new HttpGet(endpoint);
        return get(endpoint, get, responseHandler);
    }

    protected String get(URI endpoint, Set<Header> headers, Set<String> extraAuthHeaders, ResponseHandler<String> responseHandler) {
        HttpGet get = new HttpGet(endpoint);
        headers.forEach(get::addHeader);
        extraAuthHeaders.forEach(h -> get.addHeader(h, OIDC_AUTH_HEADER_PREFIX + getOIDCToken()));
        return get(endpoint, get, responseHandler);
    }

    protected String get(URI endpoint, HttpGet get, ResponseHandler<String> responseHandler) {
        try {
            return this.execute(get, responseHandler);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("Kunne ikke GET fra %s", endpoint), e);
        }
    }

    protected String patch(URI endpoint, Object dto, Set<Header> headers, ResponseHandler<String> responseHandler) {
        HttpPatch patch = new HttpPatch(endpoint);
        String json = toJson(dto);
        patch.setEntity(new StringEntity(json, UTF_8));
        headers.forEach(patch::addHeader);
        try {
            return this.execute(patch, responseHandler);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("Kunne ikke PATCH mot %s", endpoint), e);
        }
    }

    protected String put(URI endpoint, Object dto, Set<Header> headers, ResponseHandler<String> responseHandler) {
        HttpPut put = new HttpPut(endpoint);
        String json = toJson(dto);
        put.setEntity(new StringEntity(json, UTF_8));
        headers.forEach(put::addHeader);
        try {
            return this.execute(put, responseHandler);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("Kunne ikke PUT  mot %s", endpoint), e);
        }
    }

    protected HttpPost getJsonPost(URI endpoint, Object dto, Set<Header> headers) {
        HttpPost post = new HttpPost(endpoint);
        post.setEntity(new StringEntity(toJson(dto), UTF_8));
        headers.forEach(post::addHeader);
        return post;
    }

    protected <T> T post(URI endpoint, Object dto, ResponseHandler<T> responseHandler) {
        return post(endpoint, dto, Set.of(), responseHandler);
    }

    protected <T> T post(URI endpoint, Object dto, Set<Header> headers, ResponseHandler<T> responseHandler) {
        HttpPost post = getJsonPost(endpoint, dto, headers);
        try {
            return this.execute(post, responseHandler);
        } catch (IOException e) {
            throw new TekniskException("F-432937", String.format("Kunne ikke POST mot %s", endpoint), e);
        }
    }

    protected String toJson(Object dto) {
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw DefaultJsonMapperFeil.FACTORY.kunneIkkeSerialisereJson(e).toException();
        }
    }

    abstract String getOIDCToken();

}

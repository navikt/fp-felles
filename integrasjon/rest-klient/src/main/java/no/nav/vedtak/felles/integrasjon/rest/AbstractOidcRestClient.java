package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.DefaultJsonMapperFeil;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler.StringResponseHandler;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.StringUtils;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 */
public abstract class AbstractOidcRestClient extends CloseableHttpClient {
    private static final String AUTH_HEADER = "Authorization";
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String CALL_ID = "xCALL_ID";
    public static final String PERSONFEED_CONSUMER_ID = "nav-consumer-id";
    public static final String PERSONFEED_CALL_ID = "nav-call-id";
    public static final String NYE_HEADER_CALL_ID = "no.nav.callid";
    public static final String NYE_HEADER_CONSUMER_ID = "no.nav.consumer.id";

    private CloseableHttpClient client;

    private ObjectMapper mapper = DefaultJsonMapper.getObjectMapper();
    
    AbstractOidcRestClient(CloseableHttpClient client) {
        this.client = client;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public <T> T execute(final HttpUriRequest request,
                         final ResponseHandler<? extends T> responseHandler, final HttpContext context)
            throws IOException,
            ClientProtocolException {
        return execute(request, responseHandler, context);
    }

    public <T> T get(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }
    
    @Override
    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    @Override
    public HttpParams getParams() {
        return client.getParams();
    }

    public <T> Optional<T> getReturnsOptional(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(fromJson(entity, clazz));
    }

    public String post(URI endpoint, Object dto) {
        return post(endpoint, dto, createResponseHandler(endpoint));
    }

    public <T> T post(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createResponseHandler(endpoint));
        return fromJson(entity, clazz);
    }

    public <T> Optional<T> postReturnsOptional(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(fromJson(entity, clazz));
    }

    protected ResponseHandler<String> createResponseHandler(URI endpoint) {
        return new StringResponseHandler(endpoint);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
        request.setHeader("Accept", "application/json");

        String authHeaderValue = OIDC_AUTH_HEADER_PREFIX + getOIDCToken();
        request.setHeader(AUTH_HEADER, authHeaderValue);

        request.setHeader(MDCOperations.HTTP_HEADER_CALL_ID, MDCOperations.getCallId());
        request.setHeader(MDCOperations.HTTP_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        setObsoleteHeaders(request);

        return client.execute(target, request, context);
    }

    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw DefaultJsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }

    protected String get(URI endpoint, ResponseHandler<String> responseHandler) {
        HttpGet get = new HttpGet(endpoint);
        try {
            return this.execute(get, responseHandler);
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(OidcRestClientFeil.formatterURI(endpoint), e).toException();
        }
    }

    protected HttpPost getJsonPost(URI endpoint, Object dto) {
        HttpPost post = new HttpPost(endpoint);
        String json = toJson(dto);
        post.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        return post;
    }

    protected String post(URI endpoint, Object dto, ResponseHandler<String> responseHandler) {
        HttpPost post = getJsonPost(endpoint, dto);
        try {
            return this.execute(post, responseHandler);
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(OidcRestClientFeil.formatterURI(endpoint), e).toException();
        }
    }

    protected void setObsoleteHeaders(HttpRequest request) {
        if (!Boolean.getBoolean("disable.obsolete.mdc.http.headers")) {
            String callId = MDCOperations.getCallId();
            request.setHeader(CALL_ID, callId);
            request.setHeader(NYE_HEADER_CALL_ID, callId);
            request.setHeader(PERSONFEED_CALL_ID, callId);
            request.setHeader(PERSONFEED_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
            request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
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
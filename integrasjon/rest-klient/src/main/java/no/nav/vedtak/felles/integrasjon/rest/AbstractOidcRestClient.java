package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.StringUtils;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 */
abstract class AbstractOidcRestClient extends CloseableHttpClient {
    private static final String AUTH_HEADER = "Authorization";
    private static final String OIDC_AUTH_HEADER_PREFIX = "Bearer ";
    private static final String CALL_ID = "xCALL_ID";
    private static final String PERSONFEED_CONSUMER_ID = "nav-consumer-id";
    private static final String PERSONFEED_CALL_ID = "nav-call-id";
    private static final String NYE_HEADER_CALL_ID = "no.nav.callid";
    private static final String NYE_HEADER_CONSUMER_ID = "no.nav.consumer.id";


    private CloseableHttpClient client;

    AbstractOidcRestClient(CloseableHttpClient client) {
        this.client = client;
    }

    abstract String getOIDCToken();

    public <T> T post(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createResponseHandler(endpoint));
        return JsonMapper.fromJson(entity, clazz);
    }


    public <T> T get(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createResponseHandler(endpoint));
        return JsonMapper.fromJson(entity, clazz);
    }

    public <T> Optional<T> postReturnsOptional(URI endpoint, Object dto, Class<T> clazz) {
        String entity = post(endpoint, dto, createResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(JsonMapper.fromJson(entity, clazz));
    }

    public <T> Optional<T> getReturnsOptional(URI endpoint, Class<T> clazz) {
        String entity = get(endpoint, createResponseHandler(endpoint));
        if (StringUtils.nullOrEmpty(entity)) {
            return Optional.empty();
        }
        return Optional.of(JsonMapper.fromJson(entity, clazz));
    }

    public String post(URI endpoint, Object dto) {
        return post(endpoint, dto, createResponseHandler(endpoint));
    }

    private String post(URI endpoint, Object dto, ResponseHandler<String> responseHandler) {
        HttpPost post = getJsonPost(endpoint, dto);
        try {
            return this.execute(post, responseHandler);
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(OidcRestClientFeil.formatterURI(endpoint), e).toException();
        }
    }

    private String get(URI endpoint, ResponseHandler<String> responseHandler) {
        HttpGet get = new HttpGet(endpoint);
        try {
            return this.execute(get, responseHandler);
        } catch (IOException e) {
            throw OidcRestClientFeil.FACTORY.ioException(OidcRestClientFeil.formatterURI(endpoint), e).toException();
        }
    }

    private HttpPost getJsonPost(URI endpoint, Object dto) {
        HttpPost post = new HttpPost(endpoint);
        String json = JsonMapper.toJson(dto);
        post.setEntity(new StringEntity(json, Charset.forName("UTF-8")));
        return post;
    }

    private ResponseHandler<String> createResponseHandler(URI endpoint) {
        return new OidcRestClientResponseHandler(endpoint);
    }

    private static class OidcRestClientResponseHandler implements ResponseHandler<String> {


        private URI endpoint;

        OidcRestClientResponseHandler(URI endpoint) {
            this.endpoint = endpoint;
        }

        public String handleResponse(final HttpResponse response) throws IOException {
            int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            } else if (status == HttpStatus.SC_FORBIDDEN) {
                throw OidcRestClientFeil.FACTORY.manglerTilgang(OidcRestClientFeil.formatterURI(endpoint)).toException();
            } else {
                throw OidcRestClientFeil.FACTORY.serverSvarteMedFeilkode(
                        OidcRestClientFeil.formatterURI(endpoint),
                        status,
                        response.getStatusLine().getReasonPhrase()
                ).toException();
            }
        }

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

    private void setObsoleteHeaders(HttpRequest request) {
        if (!Boolean.getBoolean("disable.obsolete.mdc.http.headers")) {
            String callId = MDCOperations.getCallId();
            request.setHeader(CALL_ID, callId);
            request.setHeader(NYE_HEADER_CALL_ID, callId);
            request.setHeader(PERSONFEED_CALL_ID, callId);
            request.setHeader(PERSONFEED_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
            request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public HttpParams getParams() {
        return client.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return client.getConnectionManager();
    }

    interface OidcRestClientFeil extends DeklarerteFeil {

        OidcRestClientFeil FACTORY = FeilFactory.create(OidcRestClientFeil.class);

        @ManglerTilgangFeil(feilkode = "F-468815", feilmelding = "Mangler tilgang. Fikk http-kode 403 fra server [%s]", logLevel = LogLevel.ERROR)
        Feil manglerTilgang(String endpoint);

        @IntegrasjonFeil(feilkode = "F-686912", feilmelding = "Server [%s] svarte med feilkode http-kode '%s' og response var '%s'", logLevel = LogLevel.WARN)
        Feil serverSvarteMedFeilkode(String endpoint, int feilkode, String feilmelding);

        @TekniskFeil(feilkode = "F-432937", feilmelding = "IOException ved kommunikasjon med server [%s]", logLevel = LogLevel.WARN)
        Feil ioException(String url, IOException cause);

        static String formatterURI(URI uri) {
            if (uri == null) {
                return "URI er ikke satt";
            }
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() >= 0 ? ":" + uri.getPort() : "") + uri.getPath();
        }
    }
}
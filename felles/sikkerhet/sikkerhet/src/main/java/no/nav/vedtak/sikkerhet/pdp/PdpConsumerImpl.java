package no.nav.vedtak.sikkerhet.pdp;

import static no.nav.vedtak.sikkerhet.pdp.feil.PdpSystemPropertyChecker.getSystemProperty;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.pdp.feil.PdpFeil;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

@ApplicationScoped
public class PdpConsumerImpl implements PdpConsumer {

    static final String PDP_ENDPOINT_URL_KEY = "abac.pdp.endpoint.url";
    static final int MAX_TOTAL_CONNECTIONS = 20;
    static final String SYSTEMBRUKER_USERNAME = "systembruker.username";
    static final String SYSTEMBRUKER_PASSWORD = "systembruker.password"; // NOSONAR
    private static final String MEDIA_TYPE = "application/xacml+json";
    private static final Logger LOG = LoggerFactory.getLogger(PdpConsumerImpl.class);

    private final CloseableHttpClient httpclient;
    private HttpClientContext localContext;
    private HttpHost target;

    public PdpConsumerImpl() {
        @SuppressWarnings("resource")
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);

        RequestConfig requestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setAuthenticationEnabled(true)
            .build();

        // FIXME: Hvorfor injectes ikke disse propertyene også?
        String brukernavn = getSystemProperty(SYSTEMBRUKER_USERNAME);
        String passord = getSystemProperty(SYSTEMBRUKER_PASSWORD);

        target = HttpHost.create(getSchemaAndHostFromURL());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials(brukernavn, passord));

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(target, basicAuth);

        localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        httpclient = HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultCredentialsProvider(credsProvider)
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    @Override
    public XacmlResponseWrapper evaluate(XacmlRequestBuilder request) {
        return execute(request);
    }

    private XacmlResponseWrapper execute(XacmlRequestBuilder request) {
        HttpPost post = new HttpPost(getSystemProperty(PDP_ENDPOINT_URL_KEY));
        post.setHeader("Content-type", MEDIA_TYPE);
        JsonObject json = request.build();
        LOG.trace("PDP-request: {}", json);
        post.setEntity(new StringEntity(json.toString(), Charset.forName("UTF-8")));
        try (CloseableHttpResponse response = httpclient.execute(target, post, localContext)) {
            final StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                final HttpEntity entity = response.getEntity();
                try (JsonReader reader = Json.createReader(entity.getContent())) {
                    JsonObject jsonResponse = reader.readObject();
                    LOG.trace("PDP-response: {}", jsonResponse);
                    return new XacmlResponseWrapper(jsonResponse);
                }
            }
            throw PdpFeil.FACTORY.httpFeil(statusLine.getStatusCode(), statusLine.getReasonPhrase()).toException();
        } catch (IOException e) {
            throw PdpFeil.FACTORY.ioFeil(e).toException();
        } finally {
            post.releaseConnection();
        }
    }

    final String getSchemaAndHostFromURL() {
        final String pdpUrl = getSystemProperty(PDP_ENDPOINT_URL_KEY);
        try {
            URI uri = new URI(pdpUrl);
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > -1 ? ":" + uri.getPort() : "");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

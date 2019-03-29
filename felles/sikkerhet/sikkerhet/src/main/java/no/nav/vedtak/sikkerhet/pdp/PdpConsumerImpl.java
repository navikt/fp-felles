package no.nav.vedtak.sikkerhet.pdp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

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

import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.pdp.feil.PdpFeil;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;
import no.nav.vedtak.util.Tuple;

@ApplicationScoped
public class PdpConsumerImpl implements PdpConsumer {

    private static final String PDP_ENDPOINT_URL_KEY = "abac.pdp.endpoint.url";
    private static final String SYSTEMBRUKER_USERNAME = "systembruker.username";
    private static final String SYSTEMBRUKER_PASSWORD = "systembruker.password"; // NOSONAR
    private static final int MAX_TOTAL_CONNECTIONS = 20;
    private static final String MEDIA_TYPE = "application/xacml+json";
    private static final Logger LOG = LoggerFactory.getLogger(PdpConsumerImpl.class);

    private String pdpUrl;
    private String brukernavn;
    private String passord;
    private HttpHost target;

    private Tuple<CloseableHttpClient, AuthCache> activeConfiguration;

    PdpConsumerImpl() {
    } //CDI

    @Inject
    public PdpConsumerImpl(@KonfigVerdi(PDP_ENDPOINT_URL_KEY) String pdpUrl, @KonfigVerdi(SYSTEMBRUKER_USERNAME) String brukernavn, @KonfigVerdi(SYSTEMBRUKER_PASSWORD) String passord) {
        this.pdpUrl = pdpUrl;
        this.brukernavn = brukernavn;
        this.passord = passord;
        target = HttpHost.create(getSchemaAndHostFromURL(pdpUrl));
        activeConfiguration = buildClient();
    }

    private Tuple<CloseableHttpClient, AuthCache> buildClient() {
        @SuppressWarnings("resource")
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()), new UsernamePasswordCredentials(brukernavn, passord));

        RequestConfig requestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setAuthenticationEnabled(true)
            .build();

        CloseableHttpClient client = HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultCredentialsProvider(credsProvider)
            .setDefaultRequestConfig(requestConfig)
            .build();

        AuthCache authCache = new BasicAuthCache();
        authCache.put(target, new BasicScheme());

        return new Tuple<>(client, authCache);
    }

    @Override
    public XacmlResponseWrapper evaluate(XacmlRequestBuilder request) {
        return new XacmlResponseWrapper(execute(request.build()));
    }

    JsonObject execute(JsonObject request) {
        HttpPost post = new HttpPost(pdpUrl);
        post.setHeader("Content-type", MEDIA_TYPE);
        post.setEntity(new StringEntity(request.toString(), Charset.forName("UTF-8")));
        LOG.trace("PDP-request: {}", request);

        Tuple<CloseableHttpClient, AuthCache> active = activeConfiguration;

        Tuple<StatusLine, JsonObject> response = call(active, post);
        int statusCode = response.getElement1().getStatusCode();
        if (HttpStatus.SC_OK == statusCode) {
            return response.getElement2();
        }
        if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
            synchronized (this) {
                if (active == activeConfiguration) {
                    activeConfiguration = buildClient();
                    PdpFeil.FACTORY.reinstansiertHttpClient().log(LOG);
                }
            }
            active = activeConfiguration;

            response = call(active, post);
            statusCode = response.getElement1().getStatusCode();
            if (HttpStatus.SC_OK == statusCode) {
                return response.getElement2();
            }
            if (HttpStatus.SC_UNAUTHORIZED == statusCode) {
                throw PdpFeil.FACTORY.autentiseringFeilerEtterReinstansiering(System.getenv("HOSTNAME")).toException();
            }
        }
        throw PdpFeil.FACTORY.httpFeil(statusCode, response.getElement1().getReasonPhrase()).toException();

    }

    private Tuple<StatusLine, JsonObject> call(Tuple<CloseableHttpClient, AuthCache> active, HttpPost post) {
        final CloseableHttpClient client = active.getElement1();
        final AuthCache authCache = active.getElement2();

        HttpClientContext context = HttpClientContext.create();
        context.setAuthCache(authCache);
        try (CloseableHttpResponse response = client.execute(target, post, context)) {
            final StatusLine statusLine = response.getStatusLine();
            if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                final HttpEntity entity = response.getEntity();
                try (JsonReader reader = Json.createReader(entity.getContent())) {
                    JsonObject jsonResponse = reader.readObject();
                    LOG.trace("PDP-response: {}", jsonResponse);
                    return new Tuple<>(statusLine, jsonResponse);
                }
            }
            return new Tuple<>(statusLine, JsonValue.EMPTY_JSON_OBJECT);
        } catch (IOException e) {
            throw PdpFeil.FACTORY.ioFeil(e).toException();
        } finally {
            post.releaseConnection();
        }
    }

    private String getSchemaAndHostFromURL(String pdpUrl) {
        try {
            URI uri = new URI(pdpUrl);
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > -1 ? ":" + uri.getPort() : "");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

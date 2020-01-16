package no.nav.vedtak.sikkerhet.pdp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
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
    private static final int MAX_TOTAL_CONNECTIONS_PER_ROUTE = 20;
    private static final String MEDIA_TYPE = "application/xacml+json";
    private static final Logger LOG = LoggerFactory.getLogger(PdpConsumerImpl.class);

    private String pdpUrl;
    private String brukernavn;
    private String passord;
    private HttpHost target;

    private Tuple<CloseableHttpClient, AuthCache> activeConfiguration;

    PdpConsumerImpl() {
    } // CDI

    @Inject
    public PdpConsumerImpl(@KonfigVerdi(PDP_ENDPOINT_URL_KEY) String pdpUrl, @KonfigVerdi(SYSTEMBRUKER_USERNAME) String brukernavn,
                           @KonfigVerdi(SYSTEMBRUKER_PASSWORD) String passord) {
        this.pdpUrl = pdpUrl;
        this.brukernavn = brukernavn;
        this.passord = passord;
        target = HttpHost.create(getSchemaAndHostFromURL(pdpUrl));
        activeConfiguration = buildClient();
    }

    @SuppressWarnings("resource")
    private Tuple<CloseableHttpClient, AuthCache> buildClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultMaxPerRoute(MAX_TOTAL_CONNECTIONS_PER_ROUTE);
        cm.setMaxTotal(3 * MAX_TOTAL_CONNECTIONS_PER_ROUTE); // i tilfelle redirects

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
            .setRetryHandler(new StandardHttpRequestRetryHandler()) // idempotente HTTP kall kan gjentas
            .setKeepAliveStrategy(createKeepAliveStrategy(30)) // keep max 30 sek keepalive på connections hvis ikke angitt av server
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
        post.setEntity(new StringEntity(request.toString(), java.nio.charset.StandardCharsets.UTF_8));

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
                    // reset client bør sjelden skje, skal ikke havne i situasjonen at vi spør etter ting de ikke er authorized til, men 
                    // det skjer at PDP server f.eks. er resatt og eneste vi kan gjøre er å resette vår egen tilstand.
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
                throw PdpFeil.FACTORY.autentiseringFeilerEtterReinstansiering(System.getenv("HOSTNAME")).toException(); // NOSONAR
            }
        }
        throw PdpFeil.FACTORY.httpFeil(statusCode, response.getElement1().getReasonPhrase()).toException();

    }

    @SuppressWarnings("resource")
    private Tuple<StatusLine, JsonObject> call(Tuple<CloseableHttpClient, AuthCache> active, HttpPost post) {
        final CloseableHttpClient client = active.getElement1();
        final AuthCache authCache = active.getElement2();

        int retries = 2;
        StatusLine statusLine = null;
        while (--retries >= 0) {
            HttpClientContext context = HttpClientContext.create();
            context.setAuthCache(authCache);
            try (CloseableHttpResponse response = client.execute(target, post, context)) {
                statusLine = response.getStatusLine();
                if (HttpStatus.SC_OK == statusLine.getStatusCode()) {
                    final HttpEntity entity = response.getEntity();
                    try (JsonReader reader = Json.createReader(entity.getContent())) {
                        JsonObject jsonResponse = reader.readObject();
                        LOG.trace("PDP-response: {}", jsonResponse);
                        return new Tuple<>(statusLine, jsonResponse);
                    }
                }
                break;
            } catch (IOException e) {
                if (retries > 0) {
                    // ved IO feil gjør et forsøk til (server kan ha stengt conn)
                    // logg kun første gang vi treffer, kast exception andre gang
                    LOG.trace("Fikk IOException - PDP feil, prøver en gang til", e);
                } else {
                    throw PdpFeil.FACTORY.ioFeil(e).toException();
                }
            } finally {
                post.releaseConnection();
            }
        }
        
        return new Tuple<>(statusLine, JsonValue.EMPTY_JSON_OBJECT);
    }

    private String getSchemaAndHostFromURL(String pdpUrl) {
        try {
            URI uri = new URI(pdpUrl);
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > -1 ? ":" + uri.getPort() : "");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Sørger for å droppe og starte nye connections innimellom også om server ikke sender keepalive header. */
    private static ConnectionKeepAliveStrategy createKeepAliveStrategy(int seconds) {
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000L;
                    }
                }
                return seconds * 1000L;
            }
        };
        return myStrategy;
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.httpcomponents.PoolingHttpClientConnectionManagerMetricsBinder;
import no.nav.vedtak.felles.integrasjon.rest.jersey.HeaderLoggingRequestInterceptor;
import no.nav.vedtak.felles.integrasjon.rest.jersey.HeaderLoggingResponseInterceptor;

@ApplicationScoped
public class RestClientSupportProdusent {

    private final OidcRestClient oidcRestClient;
    private final SystemUserOidcRestClient systemUserOidcRestClient;

    public RestClientSupportProdusent() {
        this.oidcRestClient = createOidcRestClient();
        this.systemUserOidcRestClient = creatSystemUserOidcRestClient();
    }

    /**
     * Sørger for å droppe og starte nye connections innimellom også om server ikke
     * sender keepalive header.
     */
    public static ConnectionKeepAliveStrategy createKeepAliveStrategy(int seconds) {
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));
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

    @Produces
    public OidcRestClient getOidcRestClient() {
        return oidcRestClient;
    }

    @Produces
    public SystemUserOidcRestClient getSystemUserOidcRestClient() {
        return systemUserOidcRestClient;
    }

    private OidcRestClient createOidcRestClient() {
        return new OidcRestClient(createHttpClient());
    }

    private SystemUserOidcRestClient creatSystemUserOidcRestClient() {
        return new SystemUserOidcRestClient(createHttpClient());
    }

    public static CloseableHttpClient createHttpClient() {
        // Create connection configuration

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = connectionManager();

        // Create default headers

        // Create an HttpClient with the given custom dependencies and configuration.
        return HttpClients.custom()
                .addInterceptorFirst(new HeaderLoggingResponseInterceptor())
                .addInterceptorLast(new HeaderLoggingRequestInterceptor())
                .setSSLHostnameVerifier(new DefaultHostnameVerifier())
                .setConnectionManager(connManager)
                .setDefaultHeaders(defaultHeaders())
                .setDefaultRequestConfig(requestConfig())
                .setRetryHandler(new HttpRequestRetryHandler())
                .setKeepAliveStrategy(createKeepAliveStrategy(30))
                .build();
    }

    private static ConnectionConfig defaultConnectionConfig() {
        return ConnectionConfig.custom()
                .setCharset(Consts.UTF_8)
                .build();
    }

    public static RequestConfig requestConfig() {
        return RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
    }

    public static List<Header> defaultHeaders() {
        return List.of(
                new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));
    }

    public static PoolingHttpClientConnectionManager connectionManager() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(55, TimeUnit.MINUTES);
        connManager.setMaxTotal(100);
        connManager.setDefaultConnectionConfig(defaultConnectionConfig());
        connManager.setValidateAfterInactivity(30000);
        new PoolingHttpClientConnectionManagerMetricsBinder(connManager, "jersey-pool").bindTo(Metrics.globalRegistry);
        return connManager;
    }

}

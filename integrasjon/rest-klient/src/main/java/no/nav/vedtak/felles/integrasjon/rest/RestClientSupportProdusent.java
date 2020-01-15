package no.nav.vedtak.felles.integrasjon.rest;

import java.util.Arrays;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

@ApplicationScoped
public class RestClientSupportProdusent {

    private final OidcRestClient oidcRestClient;
    private final SystemUserOidcRestClient systemUserOidcRestClient;

    public RestClientSupportProdusent() {
        this.oidcRestClient = createOidcRestClient();
        this.systemUserOidcRestClient = creatSystemUserOidcRestClient();
    }

    /**
     * Sørger for å droppe og starte nye connections innimellom også om server ikke sender keepalive header.
     */
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

    @Produces
    public OidcRestClient getOidcRestClient() {
        return oidcRestClient;
    }

    @Produces
    public SystemUserOidcRestClient getSystemUserOidcRestClient() {
        return systemUserOidcRestClient;
    }

    @SuppressWarnings("resource")
    private OidcRestClient createOidcRestClient() {
        CloseableHttpClient closeableHttpClient = createHttpClient();
        return new OidcRestClient(closeableHttpClient);
    }

    @SuppressWarnings("resource")
    private SystemUserOidcRestClient creatSystemUserOidcRestClient() {
        CloseableHttpClient closeableHttpClient = createHttpClient();
        return new SystemUserOidcRestClient(closeableHttpClient);
    }

    @SuppressWarnings("resource")
    private CloseableHttpClient createHttpClient() {
        // Create connection configuration
        ConnectionConfig defaultConnectionConfig = ConnectionConfig.custom()
            .setCharset(Consts.UTF_8)
            .build();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(55, TimeUnit.MINUTES);
        connManager.setMaxTotal(100);
        connManager.setDefaultConnectionConfig(defaultConnectionConfig);
        connManager.setValidateAfterInactivity(100);

        // Create global request configuration
        RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build();

        // Create default headers
        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        List<Header> defaultHeaders = Arrays.asList(header);

        // Create an HttpClient with the given custom dependencies and configuration.
        return HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultHeaders(defaultHeaders)
            .setDefaultRequestConfig(defaultRequestConfig)
            .setRetryHandler(new HttpRequestRetryHandler())
            .setKeepAliveStrategy(createKeepAliveStrategy(30))
            .build();
    }

}

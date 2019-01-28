package no.nav.vedtak.felles.integrasjon.rest;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

@ApplicationScoped
public class RestClientSupportProdusent {

    private final OidcRestClient oidcRestClient;
    private final SystemUserOidcRestClient systemUserOidcRestClient;

    public RestClientSupportProdusent() {
        this.oidcRestClient = createOidcRestClient();
        this.systemUserOidcRestClient = creatSystemUserOidcRestClient();
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
        CloseableHttpClient closeableHttpClient = createHttpClient();
        return new OidcRestClient(closeableHttpClient);
    }

    private SystemUserOidcRestClient creatSystemUserOidcRestClient() {
        CloseableHttpClient closeableHttpClient = createHttpClient();
        return new SystemUserOidcRestClient(closeableHttpClient);
    }

    private CloseableHttpClient createHttpClient() {
        // Create connection configuration
        ConnectionConfig defaultConnectionConfig = ConnectionConfig.custom()
                .setCharset(Consts.UTF_8)
                .build();

        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(100);
        connManager.setDefaultConnectionConfig(defaultConnectionConfig);

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
                .build();
    }

}
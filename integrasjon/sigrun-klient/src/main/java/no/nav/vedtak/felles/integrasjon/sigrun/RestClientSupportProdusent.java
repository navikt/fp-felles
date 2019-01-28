package no.nav.vedtak.felles.integrasjon.sigrun;

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

    private final SigrunRestClient sigrunRestClient;

    public RestClientSupportProdusent() {
        this.sigrunRestClient = createSigrunRestClient();
    }

    @Produces
    public SigrunRestClient getSigrunRestClient() {
        return sigrunRestClient;
    }

    private SigrunRestClient createSigrunRestClient() {
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
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultHeaders(defaultHeaders)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        return new SigrunRestClient(closeableHttpClient);
    }

}

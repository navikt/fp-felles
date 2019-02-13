package no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class MottaInngaaendeForsendelseRestSelftestKlientImpl implements MottaInngaaendeForsendelseRestSelftestKlient {

    private static final String MOTTA_INNGAAENDE_FORSENDELSE_URL = "mottaInngaaendeForsendelse.url";
    private static final String SELFTEST_PATH = "isReady";

    private CloseableHttpClient restClient;
    private URI endpointUrl;

    @Inject
    public MottaInngaaendeForsendelseRestSelftestKlientImpl(@KonfigVerdi(MOTTA_INNGAAENDE_FORSENDELSE_URL) URI endpointUrl) {
        this.endpointUrl = utledSelftestEndpoint(endpointUrl);
        this.restClient = createClient();
    }

    @Override
    public void ping() {
        try {
            CloseableHttpResponse response = restClient.execute(new HttpGet(this.endpointUrl));
            int status = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK != status) {
                throw MottaInngaaendeForsendelseRestSelftestFeil.FACTORY.serverSvarteMedFeilkode(status, response.getStatusLine().getReasonPhrase()).toException();
            }
        } catch (IOException e) {
            throw MottaInngaaendeForsendelseRestSelftestFeil.FACTORY.ioException(e).toException();
        }
    }

    @Override
    public String getEndpointUrl() {
        return endpointUrl.normalize().toString();
    }

    private URI utledSelftestEndpoint(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();

        try {
            return new URIBuilder()
                    .setScheme(scheme)
                    .setHost(host)
                    .setPort(port)
                    .setPath(SELFTEST_PATH)
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private CloseableHttpClient createClient() {
        ConnectionConfig defaultConnConfig = ConnectionConfig.custom()
                .setCharset(Charset.forName("UTF-8"))
                .build();

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(100);
        connManager.setDefaultConnectionConfig(defaultConnConfig);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();

        Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        List<Header> defaultHeaders = Arrays.asList(header);

        return HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultHeaders(defaultHeaders)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
    }

    interface MottaInngaaendeForsendelseRestSelftestFeil extends DeklarerteFeil {
        MottaInngaaendeForsendelseRestSelftestFeil FACTORY = FeilFactory.create(MottaInngaaendeForsendelseRestSelftestFeil.class);

        @IntegrasjonFeil(feilkode = "F-647280", feilmelding = "Server svarte med feilkode http-kode '%s' og response var '%s'", logLevel = LogLevel.WARN)
        Feil serverSvarteMedFeilkode(int feilkode, String feilmelding);

        @TekniskFeil(feilkode = "F-647281", feilmelding = "IOException ved kommunikasjon med server", logLevel = LogLevel.WARN)
        Feil ioException(IOException cause);
    }
}

package no.nav.vedtak.klient.http;

import no.nav.foreldrepenger.konfig.Environment;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

public final class ProxyHttpClient extends BaseHttpClient {
    private static final Environment ENV = Environment.current();

    private static final String AZURE_HTTP_PROXY = "azure.http.proxy";
    private static final String PROXY_KEY = "proxy.url";
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";

    private static ProxyHttpClient CLIENT;

    private ProxyHttpClient() {
        super(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .proxy(Optional.ofNullable(ENV.isFss() ? URI.create(ENV.getProperty(AZURE_HTTP_PROXY, getDefaultProxy())) : null)
            .map(p -> new InetSocketAddress(p.getHost(), p.getPort()))
            .map(ProxySelector::of)
            .orElse(HttpClient.Builder.NO_PROXY)).build());
    }

    public static synchronized ProxyHttpClient client() {
        var inst = CLIENT;
        if (inst == null) {
            inst = new ProxyHttpClient();
            CLIENT = inst;
        }
        return inst;
    }

    private static String getDefaultProxy() {
        return ENV.getProperty(PROXY_KEY, DEFAULT_PROXY_URL);
    }
}

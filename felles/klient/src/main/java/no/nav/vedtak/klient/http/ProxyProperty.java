package no.nav.vedtak.klient.http;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Optional;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Standard navn på environment injisert av NAIS når maskinporten er enabled
 * Dvs naiserator:spec:maskinporten:enabled: true
 */
public class ProxyProperty {
    private static final Environment ENV = Environment.current();

    private static final String AZURE_HTTP_PROXY = "azure.http.proxy";
    private static final String DEFAULT_PROXY_URL = "http://webproxy.nais:8088";

    private ProxyProperty() {
    }

    public static URI getProxy() {
        return URI.create(ENV.getProperty(AZURE_HTTP_PROXY, DEFAULT_PROXY_URL));
    }

    public static URI getProxyIfFSS() {
        return ENV.isFss() ? getProxy() : null;
    }

    public static ProxySelector getProxySelector() {
        var proxy = getProxy();
        return ProxySelector.of(new InetSocketAddress(proxy.getHost(), proxy.getPort()));
    }

    public static ProxySelector getProxySelectorIfFSS() {
        return ENV.isFss() ? getProxySelector() : HttpClient.Builder.NO_PROXY;
    }

    public static ProxySelector getProxySelector(URI proxy) {
        return Optional.ofNullable(proxy)
            .map(p -> new InetSocketAddress(p.getHost(), p.getPort()))
            .map(ProxySelector::of)
            .orElse(HttpClient.Builder.NO_PROXY);
    }
}

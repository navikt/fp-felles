package no.nav.vedtak.klient.http;

import java.net.http.HttpClient;
import java.time.Duration;

public final class ProxyHttpClient extends BaseHttpClient {
    private static ProxyHttpClient CLIENT;

    private ProxyHttpClient() {
        super(HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .proxy(ProxyProperty.getProxySelectorIfFSS()).build());
    }

    public static synchronized ProxyHttpClient client() {
        var inst = CLIENT;
        if (inst == null) {
            inst = new ProxyHttpClient();
            CLIENT = inst;
        }
        return inst;
    }

}

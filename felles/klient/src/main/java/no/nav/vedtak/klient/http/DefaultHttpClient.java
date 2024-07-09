package no.nav.vedtak.klient.http;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Generic interface to using java.net.http.HttpClient using HttpKlientRequest to ensure callId and timeout
 * Will handle status > 300 by throwing exceptions, unless using sendNoResponseHandler
 * <p>
 * Usage:
 * - Create a HttpRequest.Builder with URI, Method, and custom headers.
 * - Use DefaultHttpKlient.instance().sendAccept(HttpKlientRequest.callId(builder))
 */
public final class DefaultHttpClient extends BaseHttpClient {

    private static DefaultHttpClient CLIENT;

    private DefaultHttpClient() {
        super(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).proxy(HttpClient.Builder.NO_PROXY).build());
    }

    public static synchronized DefaultHttpClient client() {
        var inst = CLIENT;
        if (inst == null) {
            inst = new DefaultHttpClient();
            CLIENT = inst;
        }
        return inst;
    }
}


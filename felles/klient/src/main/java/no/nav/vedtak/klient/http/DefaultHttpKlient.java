package no.nav.vedtak.klient.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;

public final class DefaultHttpKlient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpKlient.class);

    private static volatile DefaultHttpKlient CLIENT;

    private final HttpClient httpClient;

    private DefaultHttpKlient() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).proxy(HttpClient.Builder.NO_PROXY).build();
    }

    public static synchronized DefaultHttpKlient client() {
        var inst= CLIENT;
        if (inst == null) {
            inst = new DefaultHttpKlient();
            CLIENT = inst;
        }
        return inst;
    }

    public String send(HttpRequest request) {
        return ResponseHandler.handleResponse(doSendExpectStringRetry(request), request.uri(), Set.of());
    }

    public String send(HttpRequest request, Set<Integer> permits) {
        return ResponseHandler.handleResponse(doSendExpectStringRetry(request), request.uri(), permits);
    }

    public Optional<byte[]> sendHandleResponse(HttpRequest request) {
        return Optional.ofNullable(ResponseHandler.handleResponse(doSendExpectBytearrayRetry(request), request.uri(), Set.of()));
    }

    public HttpResponse<String> sendNoResponseHandler(HttpRequest request) {
        return doSendExpectStringRetry(request);
    }

    private HttpResponse<byte[]> doSendExpectBytearrayRetry(HttpRequest request) {
        try {
            return doSendExpectBytearray(request);
        } catch (IntegrasjonException e) {
            LOG.info("F-157390 IntegrasjonException ved første kall til endepunkt {}", request.uri(), e);
        }
        return doSendExpectBytearray(request);
    }

    private HttpResponse<String> doSendExpectStringRetry(HttpRequest request) {
        try {
            return doSendExpectString(request);
        } catch (IntegrasjonException e) {
            LOG.info("F-157390 IntegrasjonException ved første kall til endepunkt {}", request.uri(), e);
        }
        return doSendExpectString(request);
    }

    private HttpResponse<byte[]> doSendExpectBytearray(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new IntegrasjonException("F-157391", "Uventet IO-exception mot endepunkt", e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-157392", "InterruptedException ved kall mot endepunkt", e);
        }
    }

    private HttpResponse<String> doSendExpectString(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IntegrasjonException("F-157391", "Uventet IO-exception mot endepunkt", e);
        }  catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-157392", "InterruptedException ved kall mot endepunkt", e);
        }
    }

}

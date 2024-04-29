package no.nav.vedtak.klient.http;

import no.nav.vedtak.exception.IntegrasjonException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

public class BaseHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(BaseHttpClient.class);
    private static final int RETRIES = 2; // 1 attempt, the n retries

    private final HttpClient httpClient;

    public BaseHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String send(HttpClientRequest request) {
        var httpRequest = request.request();
        return ResponseHandler.handleResponse(doSendExpectStringRetry(httpRequest), httpRequest.uri(), Set.of());
    }

    public String send(HttpClientRequest request, Set<Integer> acceptStatus) {
        var httpRequest = request.request();
        return ResponseHandler.handleResponse(doSendExpectStringRetry(httpRequest), httpRequest.uri(), acceptStatus);
    }

    public byte[] sendReturnByteArray(HttpClientRequest request) {
        var httpRequest = request.request();
        return ResponseHandler.handleResponse(doSendExpectBytearrayRetry(httpRequest), httpRequest.uri(), Set.of());
    }

    public HttpResponse<String> sendReturnResponse(HttpClientRequest request) {
        var httpRequest = request.request();
        return ResponseHandler.handleRawResponse(doSendExpectStringRetry(httpRequest), httpRequest.uri(), Set.of());
    }

    public HttpResponse<String> sendReturnResponse(HttpClientRequest request, Set<Integer> acceptStatus) {
        var httpRequest = request.request();
        return ResponseHandler.handleRawResponse(doSendExpectStringRetry(httpRequest), httpRequest.uri(), acceptStatus);
    }

    public HttpResponse<byte[]> sendReturnResponseByteArray(HttpClientRequest request) {
        var httpRequest = request.request();
        return ResponseHandler.handleRawResponse(doSendExpectBytearrayRetry(httpRequest), httpRequest.uri(), Set.of());
    }

    /**
     * Raw response, not checked for status codes 4nn or 5nn - please ensure that any usage avoids "quiet errors"
     */
    public HttpResponse<String> sendReturnUnhandled(HttpClientRequest request) {
        return doSendExpectStringRetry(request.request());
    }

    public HttpResponse<String> sendReturnUnhandledNoRetry(HttpClientRequest request) {
        return doSendExpectString(request.request());
    }

    private HttpResponse<byte[]> doSendExpectBytearrayRetry(HttpRequest httpRequest) {
        int i = RETRIES;
        while (i-- > 0) {
            try {
                return doSendExpectBytearray(httpRequest);
            } catch (IntegrasjonException e) {
                LOG.trace("F-157390 IntegrasjonException ved kall {} til endepunkt {}", RETRIES - i, e);
            }
        }
        return doSendExpectBytearray(httpRequest);
    }

    private HttpResponse<String> doSendExpectStringRetry(HttpRequest httpRequest) {
        int i = RETRIES;
        while (i-- > 0) {
            try {
                return doSendExpectString(httpRequest);
            } catch (IntegrasjonException e) {
                LOG.trace("F-157390 IntegrasjonException ved kall {} til endepunkt {}", RETRIES - i, e);
            }
        }
        return doSendExpectString(httpRequest);
    }

    private HttpResponse<byte[]> doSendExpectBytearray(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new IntegrasjonException("F-157391", "Uventet IO-exception mot endepunkt", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-157392", "InterruptedException ved kall mot endepunkt", e);
        }
    }

    private HttpResponse<String> doSendExpectString(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IntegrasjonException("F-157391", "Uventet IO-exception mot endepunkt", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrasjonException("F-157392", "InterruptedException ved kall mot endepunkt", e);
        }
    }
}

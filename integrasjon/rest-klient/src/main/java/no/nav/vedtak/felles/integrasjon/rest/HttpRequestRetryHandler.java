package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

public class HttpRequestRetryHandler extends StandardHttpRequestRetryHandler {

    public HttpRequestRetryHandler() {
        super(3, false);
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (exception instanceof NoHttpResponseException && executionCount <= getRetryCount()) {
            return true;
        }

        return super.retryRequest(exception, executionCount, context);
    }
}

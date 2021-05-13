package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class HeaderLoggingResponseInterceptor implements HttpResponseInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(HeaderLoggingResponseInterceptor.class);
    private static final Environment ENV = Environment.current();

    @Override
    public void process(HttpResponse res, HttpContext context) throws HttpException, IOException {
        if (!ENV.isProd()) {
            Arrays.stream(res.getAllHeaders())
                    .forEach(e -> LOG.trace("{} -> {}", e.getName(), e.getValue()));
        }
    }
}

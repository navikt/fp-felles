package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

public class HeaderLoggingRequestInterceptor implements HttpRequestInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(HeaderLoggingRequestInterceptor.class);
    private static final Environment ENV = Environment.current();

    @Override
    public void process(HttpRequest req, HttpContext ctx) throws HttpException, IOException {
        if (!ENV.isProd()) {
            Arrays.stream(req.getAllHeaders())
                    .forEach(e -> LOG.trace("{} -> {}", e.getName(), e.getValue()));
        }
    }
}

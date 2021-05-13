package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;

@Priority(999999)
class HeaderLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderLoggingFilter.class);
    private static final Environment ENV = Environment.current();

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        LOG.trace("Request til URI {}", ctx.getUri());
        if (!ENV.isProd()) {
            ctx.getHeaders().entrySet().stream()
                    .forEach(e -> LOG.trace("{} -> {}", e.getKey(), e.getValue()));
        }
    }

    @Override
    public void filter(ClientRequestContext ctx, ClientResponseContext responseContext) throws IOException {
        LOG.trace("Respons fra URI {} er {}", ctx.getUri(), responseContext.getStatus());
        if (!ENV.isProd()) {
            ctx.getHeaders().entrySet().stream()
                    .forEach(e -> LOG.trace("{} -> {}", e.getKey(), e.getValue()));
        }
    }
}

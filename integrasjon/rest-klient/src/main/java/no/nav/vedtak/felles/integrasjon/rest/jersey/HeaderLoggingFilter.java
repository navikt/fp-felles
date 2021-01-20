package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

class HeaderLoggingFilter implements ClientRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderLoggingFilter.class);

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        if (!Environment.current().isProd()) {
            ctx.getHeaders().entrySet().stream()
                    .forEach(e -> LOG.trace("{} -> {}", e.getKey(), e.getValue()));
        }
    }
}
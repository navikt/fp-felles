package no.nav.vedtak.sikkerhet.pdp.jaxrs;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Base64.getEncoder;

import java.io.IOException;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ClientRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthFilter.class);
    private final String header;

    public BasicAuthFilter(@KonfigVerdi("systembruker.username") String user, @KonfigVerdi("systembruker.password") String pw) {
        header = getEncoder().encodeToString((user + ":" + pw).getBytes(defaultCharset()));
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add("Authorization", "Basic " + header);
        LOG.debug("Added Authorization header");
    }
}

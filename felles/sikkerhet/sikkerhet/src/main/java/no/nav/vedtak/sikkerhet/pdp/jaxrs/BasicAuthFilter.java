package no.nav.vedtak.sikkerhet.pdp.jaxrs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.util.env.Environment;

@Dependent
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ClientRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuthFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String auth = lagBasicAuthHeaderForSystembruker();
        requestContext.getHeaders().add("Authorization", "Basic " + auth);
        LOG.debug("Added Authorization header.");
    }

    protected String lagBasicAuthHeaderForSystembruker() {
        String brukernavn = Environment.current().getRequiredProperty("systembruker.username",
                () -> new TekniskException("F-461635", String.format("System property %s kan ikke være null", "systembruker.username")));
        String passord = Environment.current().getRequiredProperty("systembruker.password",
                () -> new TekniskException("F-461635", String.format("System property %s kan ikke være null", "systembruker.password")));
        String brukernavnOgPassord = brukernavn + ":" + passord;
        return Base64.getEncoder().encodeToString(brukernavnOgPassord.getBytes(Charset.defaultCharset()));
    }

}

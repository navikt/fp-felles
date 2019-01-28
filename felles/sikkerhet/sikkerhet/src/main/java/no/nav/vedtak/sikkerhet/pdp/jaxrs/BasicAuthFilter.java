package no.nav.vedtak.sikkerhet.pdp.jaxrs;

import static no.nav.vedtak.sikkerhet.pdp.feil.PdpSystemPropertyChecker.getSystemProperty;

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

@Dependent
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ClientRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BasicAuthFilter.class);

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String auth = lagBasicAuthHeaderForSystembruker();
        requestContext.getHeaders().add("Authorization", "Basic " + auth);
        log.debug("Added Authorization header.");
    }

    protected String lagBasicAuthHeaderForSystembruker() {
        String brukernavn = getSystemProperty("systembruker.username");
        String passord = getSystemProperty("systembruker.password");
        String brukernavnOgPassord = brukernavn + ":" + passord;
        return Base64.getEncoder().encodeToString(brukernavnOgPassord.getBytes(Charset.defaultCharset()));
    }

}

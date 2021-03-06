package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

@Deprecated
/**
 * 
 * Denne er erstattet til fordel for feilhåndtering i @see ExceptionTranslatingInvoker
 *
 */
class ErrorResponseHandlingClientResponseFilter implements ClientResponseFilter {

    private static final String ERRORCODE = "F-468815";
    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponseHandlingClientResponseFilter.class);

    @Override
    public void filter(ClientRequestContext req, ClientResponseContext res) throws IOException {
        var code = res.getStatus();
        switch (res.getStatusInfo().getFamily()) {
            case CLIENT_ERROR:
                if (code == FORBIDDEN.getStatusCode()) {
                	res.getEntityStream().close();
                    throw new ManglerTilgangException(ERRORCODE, "Feilet mot " + req.getUri());
                }
            	res.getEntityStream().close();
                throw new IntegrasjonException(ERRORCODE, String.format("Uventet respons %s fra %s", code, req.getUri()));
            case SERVER_ERROR:
            	res.getEntityStream().close();
                throw new IntegrasjonException(ERRORCODE, String.format("Uventet respons %s fra %s", code, req.getUri()));
            default:
                LOG.trace("Respons {} er OK", code);
        }
    }

}

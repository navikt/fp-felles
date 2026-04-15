package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.server.rest.FeilRespons;
import tools.jackson.databind.DatabindException;

public class DatabindExceptionMapper implements ExceptionMapper<DatabindException> {

    private static final Logger LOG = LoggerFactory.getLogger(DatabindExceptionMapper.class);

    @Override
    public Response toResponse(DatabindException exception) {
        FeilRespons.ensureCallId();
        var feil = "FP-252294 JSON-mapping feil";
        LOG.warn(feil, exception);
        return FeilRespons.jsonFeil(feil);
    }

}

package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.server.rest.FeilRespons;
import tools.jackson.core.exc.StreamReadException;

public class StreamReadExceptionMapper implements ExceptionMapper<StreamReadException> {

    private static final Logger LOG = LoggerFactory.getLogger(StreamReadExceptionMapper.class);

    @Override
    public Response toResponse(StreamReadException exception) {
        FeilRespons.ensureCallId();
        var feil = String.format("FP-299955 JSON-parsing feil: %s", exception.getMessage());
        LOG.warn(feil, exception);
        return FeilRespons.jsonFeil(feil);
    }


}

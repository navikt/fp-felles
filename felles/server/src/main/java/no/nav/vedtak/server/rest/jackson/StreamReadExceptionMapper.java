package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import no.nav.vedtak.server.rest.FeilUtils;
import tools.jackson.core.exc.StreamReadException;

public class StreamReadExceptionMapper implements ExceptionMapper<StreamReadException> {

    @Override
    public Response toResponse(StreamReadException exception) {
        FeilUtils.ensureCallId();
        var feil = String.format("FP-299955 JSON-parsing feil: %s", exception.getMessage());
        FeilUtils.loggWarning(feil, exception);
        return FeilUtils.jsonFeil(feil);
    }


}

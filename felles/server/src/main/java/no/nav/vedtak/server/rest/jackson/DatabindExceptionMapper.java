package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import no.nav.vedtak.server.rest.FeilUtils;
import tools.jackson.databind.DatabindException;

public class DatabindExceptionMapper implements ExceptionMapper<DatabindException> {

    @Override
    public Response toResponse(DatabindException exception) {
        FeilUtils.ensureCallId();
        var feil = "FP-252294 JSON-mapping feil";
        FeilUtils.loggWarning(feil, exception);
        return FeilUtils.jsonFeil(feil);
    }

}

package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.JsonMappingException;

import no.nav.vedtak.server.rest.FeilUtils;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    @Override
    public Response toResponse(JsonMappingException exception) {
        FeilUtils.ensureCallId();
        var feil = "FP-252294 JSON-mapping feil";
        FeilUtils.loggWarning(feil, exception);
        return FeilUtils.jsonFeil(feil);
    }


}

package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.core.JsonParseException;

import no.nav.vedtak.server.rest.FeilUtils;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    @Override
    public Response toResponse(JsonParseException exception) {
        FeilUtils.ensureCallId();
        var feil = String.format("FP-299955 JSON-parsing feil: %s", exception.getMessage());
        FeilUtils.loggWarning(feil, exception);
        return FeilUtils.jsonFeil(feil);
    }


}

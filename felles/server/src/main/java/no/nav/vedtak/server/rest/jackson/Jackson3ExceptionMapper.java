package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.server.rest.FeilUtils;
import tools.jackson.core.JacksonException;

public class Jackson3ExceptionMapper implements ExceptionMapper<JacksonException> {

    @Override
    public Response toResponse(JacksonException exception) {
        FeilUtils.ensureCallId();
        var feil = "FP-252294 JSON-feil: " + LoggerUtils.removeLineBreaks(exception.getMessage());
        FeilUtils.loggWarning(feil);
        return FeilUtils.responseFra(Response.Status.BAD_REQUEST, Feilkode.GENERELL, feil);
    }
}

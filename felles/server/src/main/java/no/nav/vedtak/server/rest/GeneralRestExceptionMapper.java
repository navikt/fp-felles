package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.MDC;


public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable feil) {
        try {
            RestServerFeilUtils.ensureCallId();
            RestServerFeilUtils.loggFeil(feil);
            var meldingTilDto = RestServerFeilUtils.getFeilmelding(feil);
            return RestServerFeilUtils.responseFra(feil, meldingTilDto);
        } finally {
            MDC.remove("prosess");
        }
    }

}

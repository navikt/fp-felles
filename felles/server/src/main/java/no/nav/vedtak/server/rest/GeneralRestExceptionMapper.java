package no.nav.vedtak.server.rest;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.MDC;


public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    @Context
    private UriInfo uriInfo;

    @Context
    private Request request;

    public GeneralRestExceptionMapper() {
        // Jersey injiserer @Context-felter
    }

    GeneralRestExceptionMapper(UriInfo uriInfo, Request request) {
        this.uriInfo = uriInfo;
        this.request = request;
    }

    @Override
    public Response toResponse(Throwable feil) {
        try {
            RestServerFeilUtils.ensureCallId();
            if (erUkjentSti(feil)) {
                RestServerFeilUtils.loggUkjentSti(request != null ? request.getMethod() : null, uriInfo.getRequestUri().getPath());
            } else {
                RestServerFeilUtils.loggFeil(feil);
            }
            var meldingTilDto = RestServerFeilUtils.getFeilmelding(feil);
            return RestServerFeilUtils.responseFra(feil, meldingTilDto);
        } finally {
            MDC.remove("prosess");
        }
    }

    // NotFoundException uten matchet ressurs betyr at Jersey ikke fant noen ressurs for stien
    private boolean erUkjentSti(Throwable feil) {
        return feil instanceof NotFoundException && uriInfo != null && uriInfo.getMatchedResources().isEmpty();
    }

}

package no.nav.vedtak.server.rest.jackson;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

import no.nav.vedtak.server.rest.FeilRespons;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        FeilRespons.ensureCallId();
        var feil = "FP-252294 JSON-mapping feil";
        LOG.warn(feil, exception);
        return FeilRespons.jsonFeil(feil);
    }


}

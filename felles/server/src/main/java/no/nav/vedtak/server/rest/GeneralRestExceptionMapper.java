package no.nav.vedtak.server.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.FeilType;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;


public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);
    private static boolean brukerRettetApplikasjon = true;



    @Override
    public Response toResponse(Throwable feil) {
        try {
            loggTilApplikasjonslogg(feil, true);
            return handleException(feil);
        } finally {
            MDC.remove("prosess");
        }
    }

    public static Response handleException(Throwable feil) {
        var meldingTilDto = getExceptionMelding(feil);
        return switch (feil) {
            case WebApplicationException wae -> Response.status(wae.getResponse().getStatus())
                .entity(new FeilDto(FeilType.GENERELL_FEIL, meldingTilDto, null))
                .type(MediaType.APPLICATION_JSON)
                .build();
            case VLException vlFeil -> Response
                .status(vlFeil.getStatusCode())
                .entity(new FeilDto(vlFeil.getFeilType(), meldingTilDto, null))
                .type(MediaType.APPLICATION_JSON)
                .build();
            default -> Response.serverError()
                .entity(new FeilDto(FeilType.GENERELL_FEIL, meldingTilDto))
                .type(MediaType.APPLICATION_JSON)
                .build();
        };
    }

    public static void loggTilApplikasjonslogg(Throwable feil, boolean skalLogge) {
        var endeligSkalLogge = skalLogge && !(feil instanceof ManglerTilgangException);
        if (endeligSkalLogge) {
            doLoggTilApplikasjonslogg(feil);
        }
    }

    public static void doLoggTilApplikasjonslogg(Throwable feil) {
        var melding = "Fikk uventet feil: " + getExceptionMelding(feil);
        LOG.warn(melding, feil);
    }


    public static String getExceptionMelding(Throwable feil) {
        var feilbeskrivelse = getTextForField(feil.getMessage());
        if (!isBrukerRettetApplikasjon() || feil instanceof ManglerTilgangException) {
            return feilbeskrivelse;
        } else if (feil instanceof FunksjonellException) {
            return String.format("Det oppstod en feil: %s.", feilbeskrivelse);
        } else {
            var callId = MDCOperations.getCallId();
            return String.format("Det oppstod en serverfeil: %s. Meld til support med referanse-id: %s", feilbeskrivelse, callId);
        }

    }

    private static String getTextForField(String input) {
        return input != null ? LoggerUtils.removeLineBreaks(input) : "";
    }

    public static boolean isBrukerRettetApplikasjon() {
        return brukerRettetApplikasjon;
    }

    // Settes false i applikasjoner som ikke skal lage en brukerorientert feilmelding (kontakt support med referanse-id)
    public static void setBrukerRettetApplikasjon(boolean brukerRettet) {
        brukerRettetApplikasjon = brukerRettet;
    }
}

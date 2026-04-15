package no.nav.vedtak.server.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.exception.VLLogLevel;
import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;


public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);
    private static boolean brukerRettetApplikasjon = true;


    @Override
    public Response toResponse(Throwable feil) {
        try {
            FeilRespons.ensureCallId();
            loggTilApplikasjonslogg(feil);
            var meldingTilDto = getResponsFeilmelding(feil);
            return FeilRespons.fra(getStatusCode(feil), getFeilkode(feil), meldingTilDto);
        } finally {
            MDC.remove("prosess");
        }
    }

    public static void loggTilApplikasjonslogg(Throwable feil) {
        var logLevel = feil instanceof VLException vlFeil ? vlFeil.getLogLevel() : VLLogLevel.WARN;
        var melding = String.format("Fikk uventet feil: %s.", getTextForField(feil.getMessage()));
        if (logLevel == null || VLLogLevel.WARN.equals(logLevel)) {
            LOG.warn(melding, feil);
        } else if (VLLogLevel.INFO.equals(logLevel)) {
            LOG.info(melding, feil);
        }
    }

    public static int getStatusCode(Throwable feil) {
        return switch (feil) {
            case WebApplicationException wae -> wae.getResponse().getStatus();
            case VLException vlFeil -> vlFeil.getStatusCode();
            default -> Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        };
    }

    public static String getFeilkode(Throwable feil) {
        return feil instanceof VLException vlFeil ? vlFeil.getFeilkode() : Feilkode.GENERELL.name();
    }

    public static String getFeilmelding(Throwable feil) {
        return getTextForField(feil.getMessage());
    }

    public static String getResponsFeilmelding(Throwable feil) {
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

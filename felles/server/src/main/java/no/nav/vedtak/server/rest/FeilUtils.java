package no.nav.vedtak.server.rest;

import java.net.HttpURLConnection;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.exception.VLLogLevel;
import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

/*
 * Hjelpemetoder for logging og håndtering av feil i REST-laget
 * - logging av feil eller melding (evt med årsak)
 * - utledning av Response - hele eller elementer (status, FeilDto)
 */
public class FeilUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FeilUtils.class);


    private FeilUtils() {
    }

    public static void ensureCallId() {
        if (MDCOperations.getCallId() == null) {
            MDCOperations.putCallId();
        }
    }

    public static void loggFeil(Throwable feil) {
        var logLevel = feil instanceof VLException vlFeil ? vlFeil.getLogLevel() : VLLogLevel.WARN;
        var melding = String.format("Fikk uventet feil: %s.", getFeilmelding(feil));
        if (logLevel == null || VLLogLevel.WARN.equals(logLevel)) {
            LOG.warn(melding, feil);
        } else if (VLLogLevel.INFO.equals(logLevel)) {
            LOG.info(melding, feil);
        }
    }

    public static void loggWarning(String loggmelding) {
        LOG.warn(loggmelding);
    }

    public static void loggWarning(String loggmelding, Throwable cause) {
        LOG.warn(loggmelding, cause);
    }

    public static Response responseFra(Throwable feil) {
        return responseFra(getStatusCode(feil), getFeilkode(feil), getFeilmelding(feil));
    }

    public static Response responseFra(Throwable feil, String feilmelding) {
        return responseFra(getStatusCode(feil), getFeilkode(feil), feilmelding);
    }

    public static Response responseFra(Response.Status status, Feilkode feilkode, String feilmelding) {
        return Response.status(status)
            .entity(feilDto(feilkode.name(), feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response responseFra(int status, Feilkode feilkode, String feilmelding) {
        return Response.status(status)
            .entity(feilDto(feilkode.name(), feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response responseFra(int status, String feilType, String feilmelding) {
        return Response.status(status)
            .entity(feilDto(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response jsonFeil(String feilmelding) {
        return responseFra(Response.Status.BAD_REQUEST, Feilkode.GENERELL, feilmelding);
    }

    public static FeilDto feilDto(String feilType, String feilmelding) {
        return new FeilDto(feilType, feilmelding, MDCOperations.getCallId());
    }

    public static boolean internFeil(Throwable feil) {
        return HttpURLConnection.HTTP_INTERNAL_ERROR == getStatusCode(feil);
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
        var input = feil.getMessage();
        return input != null ? LoggerUtils.removeLineBreaks(input) : "";
    }

}

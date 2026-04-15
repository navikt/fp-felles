package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.log.mdc.MDCOperations;


public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    private static boolean legacyFrontendInternFeil = false;

    @Override
    public Response toResponse(Throwable feil) {
        try {
            FeilUtils.ensureCallId();
            FeilUtils.loggFeil(feil);
            var meldingTilDto = getResponsFeilmelding(feil);
            return FeilUtils.responseFra(feil, meldingTilDto);
        } finally {
            MDC.remove("prosess");
        }
    }

    public static String getResponsFeilmelding(Throwable feil) {
        var feilbeskrivelse = FeilUtils.getFeilmelding(feil);
        if (feil instanceof ManglerTilgangException || feil instanceof FunksjonellException) {
            return feilbeskrivelse;
        } else if (isLegacyFrontendInternFeil() && FeilUtils.internFeil(feil)) {
            var callId = MDCOperations.getCallId();
            return String.format("Det oppstod en serverfeil: %s. Meld til support med referanse-id: %s", feilbeskrivelse, callId);
        } else {
            return feilbeskrivelse;
        }
    }

    private static boolean isLegacyFrontendInternFeil() {
        return legacyFrontendInternFeil;
    }

    // Settes false i applikasjoner som ikke skal lage en brukerorientert feilmelding (kontakt support med referanse-id)
    public static void setLegacyFrontendInternFeil(boolean brukerRettet) {
        legacyFrontendInternFeil = brukerRettet;
    }
}

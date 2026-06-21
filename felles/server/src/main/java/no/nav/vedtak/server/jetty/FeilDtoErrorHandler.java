package no.nav.vedtak.server.jetty;

import static no.nav.vedtak.klient.http.CommonHttpHeaders.HEADER_NAV_ALT_CALLID;
import static no.nav.vedtak.klient.http.CommonHttpHeaders.HEADER_NAV_CALLID;
import static no.nav.vedtak.klient.http.CommonHttpHeaders.HEADER_NAV_LOWER_CALL_ID;

import java.io.PrintWriter;
import java.util.Optional;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

/**
 * Felles {@link ErrorHandler} for feil som oppstår <em>utenfor</em> Rest-containerne - typisk {@code 403} fra
 * sikkerhetsoppsettet, {@code 404} på en path uten registrert ressurs, samt uventede feil rundt servletene.
 *
 * <p>Bruker FeilDto slik at våre restklienter (Accept: application/json) kan forvente denne ved ulike typer feil.
 *
 * <p>Bare {@link #writeErrorJson} er overstyrt; HTML/plain-tekst-forhandling for andre {@code Accept}-typer
 * arves fra {@link ErrorHandler}.
 */
public class FeilDtoErrorHandler extends ErrorHandler {

    @Override
    protected void writeErrorJson(Request request, PrintWriter writer, int code, String message, Throwable cause) {
        var feil = new FeilDto(code, Feilkode.GENERELL.name(), feilmelding(code, message), callId(request));
        writer.write(DefaultJsonMapper.toJson(feil));
    }

    private static String feilmelding(int code, String message) {
        return Optional.ofNullable(message)
            .filter(s -> !s.isBlank())
            .or(() -> Optional.ofNullable(HttpStatus.getMessage(code)))
            .orElse("Ukjent feil");
    }

    private static String callId(Request request) {
        var headers = request.getHeaders();
        return header(headers, HEADER_NAV_CALLID)
            .or(() -> header(headers, HEADER_NAV_ALT_CALLID))
            .or(() -> header(headers, HEADER_NAV_LOWER_CALL_ID))
            .orElseGet(MDCOperations::generateCallId);
    }

    private static Optional<String> header(HttpFields headers, String name) {
        return Optional.ofNullable(headers.get(name)).filter(s -> !s.isBlank());
    }
}

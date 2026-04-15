package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.mdc.MDCOperations;

public class FeilRespons {

    private FeilRespons() {
    }

    public static void ensureCallId() {
        if (MDCOperations.getCallId() == null) {
            MDCOperations.putCallId();
        }
    }

    public static Response fra(Response.Status status, Feilkode feilkode, String feilmelding) {
        return Response.status(status)
            .entity(fra(feilkode.name(), feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response fra(int status, Feilkode feilkode, String feilmelding) {
        return Response.status(status)
            .entity(fra(feilkode.name(), feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response fra(int status, String feilType, String feilmelding) {
        return Response.status(status)
            .entity(fra(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    public static Response jsonFeil(String feilmelding) {
        return fra(Response.Status.BAD_REQUEST, Feilkode.GENERELL, feilmelding);
    }

    private static FeilDto fra(String feilType, String feilmelding) {
        return new FeilDto(feilType, feilmelding, MDCOperations.getCallId());
    }

}

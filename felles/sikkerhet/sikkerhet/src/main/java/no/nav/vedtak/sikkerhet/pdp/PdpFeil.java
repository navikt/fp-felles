package no.nav.vedtak.sikkerhet.pdp;

import java.io.IOException;
import java.util.List;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.pdp.xacml.Obligation;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

class PdpFeil {

    private PdpFeil() {

    }

    static TekniskException httpFeil(int status, String statusInfo) {
        return new TekniskException("F-815365", String.format("Mottok HTTP error fra PDP: HTTP %s - %s", status, statusInfo));
    }

    static TekniskException ioFeil(IOException e) {
        return new TekniskException("F-091324", "Uventet IO-exception mot PDP", e);
    }

    static TekniskException indeterminateDecisionFeil(Decision originalDecision, XacmlResponseWrapper response) {
        return new TekniskException("F-080281",
                String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", originalDecision, response));
    }

    static TekniskException ukjentObligationsFeil(List<Obligation> obligations) {
        return new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
    }

    static TekniskException reinstansiertHttpClient() {
        return new TekniskException("F-563467", "Feilet autentisering mot PDP, reinstansierer hele klienten for å fjerne all state");
    }

    static TekniskException autentiseringFeilerEtterReinstansiering(String podName) {
        return new TekniskException("F-867412",
                String.format("Feilet autentisering mot PDP, reinstansiering av klienten hjalp ikke. Tiltak: Drep pod '%s'", podName));
    }
}

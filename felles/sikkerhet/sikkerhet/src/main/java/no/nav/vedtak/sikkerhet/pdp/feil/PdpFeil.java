package no.nav.vedtak.sikkerhet.pdp.feil;

import static no.nav.vedtak.feil.LogLevel.ERROR;

import java.io.IOException;
import java.util.List;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.pdp.xacml.Obligation;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

public interface PdpFeil extends DeklarerteFeil {
    PdpFeil FACTORY = FeilFactory.create(PdpFeil.class);

    @TekniskFeil(feilkode = "F-815365", feilmelding = "Mottok HTTP error fra PDP: HTTP %s - %s", logLevel = LogLevel.WARN)
    Feil httpFeil(int status, String statusInfo);

    @TekniskFeil(feilkode = "F-091324", feilmelding = "Uventet IO-exception mot PDP", logLevel = LogLevel.WARN)
    Feil ioFeil(IOException ioexception);

    @TekniskFeil(feilkode = "F-461635", feilmelding = "System property %s kan ikke være null.", logLevel = LogLevel.ERROR)
    Feil propertyManglerFeil(String key);

    @TekniskFeil(feilkode = "F-080281", feilmelding = "Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", logLevel = ERROR)
    Feil indeterminateDecisionFeil(Decision originalDecision, XacmlResponseWrapper response);

    @TekniskFeil(feilkode = "F-576027", feilmelding = "Mottok ukjente obligations fra PDP: %s", logLevel = ERROR)
    Feil ukjentObligationsFeil(List<Obligation> obligations);
}

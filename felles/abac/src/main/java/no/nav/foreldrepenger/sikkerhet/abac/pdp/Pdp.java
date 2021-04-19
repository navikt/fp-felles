package no.nav.foreldrepenger.sikkerhet.abac.pdp;

import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;

public interface Pdp {
    Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest);
}

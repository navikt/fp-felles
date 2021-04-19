package no.nav.foreldrepenger.sikkerhet.abac.pep;

import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;

public interface Pep {

    Tilgangsbeslutning vurderTilgang(BeskyttRessursAttributer attributter);
}

package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public interface PdpKlient {

    Tilgangsbeslutning forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData);

}

package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public interface PdpKlient {

    /**
     * Key i PdpRequest hvor token informasjon ligger.
     */
    String ENVIRONMENT_AUTH_TOKEN = "no.nav.vedtak.sikkerhet.pdp.AbacIdToken";

    Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest);

    Tilgangsbeslutning forespørTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, String domene, AppRessursData appRessursData);

}

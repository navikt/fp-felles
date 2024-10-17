package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public record Tilgangsbeslutning(AbacResultat beslutningKode,
                                 BeskyttetRessursAttributter beskyttetRessursAttributter,
                                 AppRessursData appRessursData) {

    public boolean fikkTilgang() {
        return beslutningKode == AbacResultat.GODKJENT;
    }
}

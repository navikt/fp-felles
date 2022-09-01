package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public record Tilgangsbeslutning(AbacResultat beslutningKode, PdpRequest pdpRequest, BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {

    public Tilgangsbeslutning(AbacResultat beslutningKode, PdpRequest pdpRequest) {
        this(beslutningKode, pdpRequest, null, null);
    }

    public Tilgangsbeslutning(AbacResultat beslutningKode, BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessursData) {
        this(beslutningKode, null, beskyttetRessursAttributter, appRessursData);
    }

    public boolean fikkTilgang() {
        return beslutningKode == AbacResultat.GODKJENT;
    }
}

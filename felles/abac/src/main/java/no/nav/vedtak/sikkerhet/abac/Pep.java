package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

public interface Pep {

    AppRessursData hentRessurser(BeskyttetRessursAttributter beskyttetRessursAttributter);

    TilgangResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter, AppRessursData appRessurser);

}

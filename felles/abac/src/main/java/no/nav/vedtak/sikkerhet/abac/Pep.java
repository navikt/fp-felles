package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.tilgang.TilgangResultat;

public interface Pep {

    TilgangResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter);

}

package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

public interface Pep {

    AbacResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter);

}

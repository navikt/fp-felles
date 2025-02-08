package no.nav.vedtak.sikkerhet.abac;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

public interface Pep {

    AbacResultat vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter);

    default String pepId() {
        return Environment.current().getNaisAppName();
    }
}

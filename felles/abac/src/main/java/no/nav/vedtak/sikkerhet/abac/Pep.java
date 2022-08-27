package no.nav.vedtak.sikkerhet.abac;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;

public interface Pep {

    default Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter) {
        throw new IllegalArgumentException("Utviklerfeil mangler impl av Pep-metode");
    }

    Tilgangsbeslutning vurderTilgang(BeskyttetRessursAttributter beskyttetRessursAttributter);

    default boolean nyttAbacGrensesnitt() {
        // Implementert ved å sjekke tilsvarende metode i PdpRequestBuilder
        return true;
    }

    default String pepId() {
        return Environment.current().getNaisAppName();
    }
}

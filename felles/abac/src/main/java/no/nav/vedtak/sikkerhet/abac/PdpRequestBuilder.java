package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public interface PdpRequestBuilder {

    PdpRequest lagPdpRequest(AbacAttributtSamling attributter);

    default String abacDomene() {
        return "foreldrepenger";
    }

    default boolean nyttAbacGrensesnitt() {
        return false;
    }

    default AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        throw new IllegalStateException("Utviklerfeil. Må implementeres for å enable nyttAbacGrensesnitt");
    }

}

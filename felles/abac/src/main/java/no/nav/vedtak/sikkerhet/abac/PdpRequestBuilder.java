package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

public interface PdpRequestBuilder {

    default String abacDomene() {
        return "foreldrepenger";
    }

    AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter);

}

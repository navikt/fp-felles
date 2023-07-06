package no.nav.vedtak.sikkerhet.abac;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

@Dependent
@Alternative
@Priority(1)
class DummyRequestBuilder implements PdpRequestBuilder {
    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter attributter) {
        return AppRessursData.builder().build();
    }
}

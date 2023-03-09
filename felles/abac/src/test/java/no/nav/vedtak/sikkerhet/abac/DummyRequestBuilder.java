package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

@Dependent
@Alternative
@Priority(1)
class DummyRequestBuilder implements PdpRequestBuilder {
    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter attributter) {
        return AppRessursData.builder().build();
    }
}

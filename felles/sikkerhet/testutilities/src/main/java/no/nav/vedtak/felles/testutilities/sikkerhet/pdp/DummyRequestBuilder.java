package no.nav.vedtak.felles.testutilities.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

@Dependent
@Alternative
@Priority(1)
public class DummyRequestBuilder implements PdpRequestBuilder {
    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        return null;
    }
}

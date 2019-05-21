package no.nav.vedtak.sikkerhet.abac;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

@Dependent
@Alternative
@Priority(1)
public class DummyRequestBuilder implements PdpRequestBuilder {
    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        return new PdpRequest();
    }
}

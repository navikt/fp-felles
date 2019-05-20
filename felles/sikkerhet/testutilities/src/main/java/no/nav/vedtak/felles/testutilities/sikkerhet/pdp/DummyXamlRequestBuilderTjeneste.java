package no.nav.vedtak.felles.testutilities.sikkerhet.pdp;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XamlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@Dependent
@Alternative
@Priority(1)
public class DummyXamlRequestBuilderTjeneste implements XamlRequestBuilderTjeneste {

    @Override
    public XacmlRequestBuilder lagXamlRequestBuilder(PdpRequest pdpRequest) {
        return new XacmlRequestBuilder();
    }
}

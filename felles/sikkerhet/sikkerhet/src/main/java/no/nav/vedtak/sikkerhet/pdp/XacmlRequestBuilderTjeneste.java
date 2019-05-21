package no.nav.vedtak.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

public interface XacmlRequestBuilderTjeneste {
    /**
     * Legger på de attributter som trengs for vurdering av abac-policy
     *
     * @param pdpRequest attributter som systemet har plukket ut som relevant for requestet
     * @return XacmlRequestBuilder
     */
    XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest);
}

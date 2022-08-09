package no.nav.vedtak.sikkerhet.pdp2;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlRequestBuilder2;

public interface XacmlRequestBuilder2Tjeneste {
    /**
     * Legger på de attributter som trengs for vurdering av abac-policy
     *
     * @param pdpRequest attributter som systemet har plukket ut som relevant for
     *                   requestet
     * @return XacmlRequestBuilder
     */
    XacmlRequestBuilder2 lagXacmlRequestBuilder2(PdpRequest req);
}

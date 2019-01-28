package no.nav.vedtak.sikkerhet.pdp.xacml;

import no.nav.vedtak.sikkerhet.abac.Decision;

public class BiasedDecisionResponse {

    private Decision biasedDecision;
    private XacmlResponseWrapper xacmlResponse;

    public BiasedDecisionResponse(Decision biasedDecision, XacmlResponseWrapper xacmlResponse) {
        this.biasedDecision = biasedDecision;
        this.xacmlResponse = xacmlResponse;
    }

    public Decision getBiasedDecision() {
        return biasedDecision;
    }

    public XacmlResponseWrapper getXacmlResponse() {
        return xacmlResponse;
    }

}

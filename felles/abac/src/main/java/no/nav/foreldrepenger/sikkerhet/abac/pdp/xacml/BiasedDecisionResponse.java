package no.nav.foreldrepenger.sikkerhet.abac.pdp.xacml;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;

public class BiasedDecisionResponse {

    private final Decision biasedDecision;
    private final XacmlResponseWrapper xacmlResponse;

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

package no.nav.vedtak.sikkerhet.pdp.xacml;

import no.nav.vedtak.sikkerhet.abac.Decision;

public record BiasedDecisionResponse(Decision biasedDecision, XacmlResponseWrapper xacmlResponse) {

    @Deprecated
    public Decision getBiasedDecision() {
        return biasedDecision();
    }

    @Deprecated
    public XacmlResponseWrapper getXacmlResponse() {
        return xacmlResponse();
    }
}

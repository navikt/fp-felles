package no.nav.vedtak.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequest;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;

public interface PdpConsumer {
    default XacmlResponse evaluate(XacmlRequestBuilder request) {
        throw new IllegalArgumentException("Utviklerfeil mangler impl av PdpConsumer-metode");
    }

    default XacmlResponse evaluate(XacmlRequest request) {
        throw new IllegalArgumentException("Utviklerfeil mangler impl av PdpConsumer-metode");
    }
}

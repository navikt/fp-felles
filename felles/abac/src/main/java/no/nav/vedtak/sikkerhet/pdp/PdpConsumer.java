package no.nav.vedtak.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;

public interface PdpConsumer {
    XacmlResponse evaluate(XacmlRequestBuilder request);
}

package no.nav.vedtak.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

public interface PdpConsumer {
    XacmlResponseWrapper evaluate(XacmlRequestBuilder request);
}

package no.nav.vedtak.sikkerhet.pdp;

import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequest;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;

public interface PdpConsumer {

    XacmlResponse evaluate(XacmlRequest request);
}

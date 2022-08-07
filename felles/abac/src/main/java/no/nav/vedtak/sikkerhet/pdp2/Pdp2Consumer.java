package no.nav.vedtak.sikkerhet.pdp2;

import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlRequestBuilder2;
import no.nav.vedtak.sikkerhet.pdp2.xacml.XacmlResponse;

public interface Pdp2Consumer {
    XacmlResponse evaluate(XacmlRequestBuilder2 request);
}

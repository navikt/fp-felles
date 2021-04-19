package no.nav.foreldrepenger.sikkerhet.abac.pdp;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequestBuilder;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponse;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponseWrapper;

public interface XacmlConsumer {
    XacmlResponseWrapper evaluate(XacmlRequestBuilder request);
    XacmlResponse evaluate(XacmlRequest request);
}

package no.nav.foreldrepenger.sikkerhet.abac.pdp;

import no.nav.foreldrepenger.sikkerhet.abac.pdp.xacml.XacmlRequestBuilder;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.xacml.XacmlResponseWrapper;

public interface XacmlConsumer {
    XacmlResponseWrapper evaluate(XacmlRequestBuilder request);
}

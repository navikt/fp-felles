package no.nav.foreldrepenger.sikkerhet.abac.pdp2;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponse;

public interface NyXacmlConsumer {
    XacmlResponse evaluate(XacmlRequest request);
}

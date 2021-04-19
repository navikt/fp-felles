package no.nav.foreldrepenger.sikkerhet.abac;

import no.nav.foreldrepenger.sikkerhet.abac.domene.BeskyttRessursAttributer;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;

public interface PdpRequestBuilder {
    PdpRequest lagPdpRequest(BeskyttRessursAttributer attributter);
}

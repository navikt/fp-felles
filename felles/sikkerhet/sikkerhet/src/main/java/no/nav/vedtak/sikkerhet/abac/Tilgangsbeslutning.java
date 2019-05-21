package no.nav.vedtak.sikkerhet.abac;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE;
import static no.nav.vedtak.util.Objects.check;

import java.util.List;

public final class Tilgangsbeslutning {
    private AbacResultat beslutningKode;
    private List<Decision> delbeslutninger;
    private PdpRequest pdpRequest;

    public Tilgangsbeslutning(AbacResultat beslutningKode, List<Decision> delbeslutninger, PdpRequest pdpRequest) {
        java.util.Objects.requireNonNull(beslutningKode);
        java.util.Objects.requireNonNull(delbeslutninger);
        java.util.Objects.requireNonNull(pdpRequest);
        int antallResources = antallResources(pdpRequest);
        check(delbeslutninger.size() == antallResources,
            String.format("Liste med decision (%d) må være like lang som liste med request til PDP (%d)", //$NON-NLS-1$
                delbeslutninger.size(),
                antallResources));

        this.beslutningKode = beslutningKode;
        this.delbeslutninger = delbeslutninger;
        this.pdpRequest = pdpRequest;
    }

    public boolean fikkTilgang() {
        return beslutningKode == AbacResultat.GODKJENT;
    }

    public AbacResultat getBeslutningKode() {
        return beslutningKode;
    }

    public List<Decision> getDelbeslutninger() {
        return delbeslutninger;
    }

    public PdpRequest getPdpRequest() {
        return pdpRequest;
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, antallAksjonspunktTyper(pdpRequest));
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }
}

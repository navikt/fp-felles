package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.util.Objects.check;

import java.util.List;

import no.nav.abac.common.xacml.CommonAttributter;
import no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter;

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
        return pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(CommonAttributter.RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }
}

package no.nav.vedtak.sikkerhet.abac;

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
        check(delbeslutninger.size() == pdpRequest.antallResources(),
                String.format("Liste med decision (%d) må være like lang som liste med request til PDP (%d)", //$NON-NLS-1$
                        delbeslutninger.size(),
                        pdpRequest.antallResources()));

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
}

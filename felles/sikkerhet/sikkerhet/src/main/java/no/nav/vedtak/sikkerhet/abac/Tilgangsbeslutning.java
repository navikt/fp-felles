package no.nav.vedtak.sikkerhet.abac;

import java.util.List;
import java.util.Objects;

public final class Tilgangsbeslutning {
    private AbacResultat beslutningKode;
    private List<Decision> delbeslutninger;
    private PdpRequest pdpRequest;

    public Tilgangsbeslutning(AbacResultat beslutningKode, List<Decision> delbeslutninger, PdpRequest pdpRequest) {
        this.beslutningKode = Objects.requireNonNull(beslutningKode);
        this.delbeslutninger = Objects.requireNonNull(delbeslutninger);
        this.pdpRequest = Objects.requireNonNull(pdpRequest);
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

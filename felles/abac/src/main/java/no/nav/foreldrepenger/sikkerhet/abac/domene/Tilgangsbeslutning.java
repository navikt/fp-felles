package no.nav.foreldrepenger.sikkerhet.abac.domene;

import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;

public final class Tilgangsbeslutning {
    private final AbacResultat beslutningKode;
    private final List<Decision> delbeslutninger;
    private final PdpRequest pdpRequest;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [beslutningKode=" + beslutningKode + ", delbeslutninger=" + delbeslutninger + ", pdpRequest="
                + pdpRequest + "]";
    }

}

package no.nav.vedtak.sikkerhet.abac;

import java.util.List;


public record Tilgangsbeslutning(AbacResultat beslutningKode, List<Decision> delbeslutninger, PdpRequest pdpRequest) {

    public boolean fikkTilgang() {
        return beslutningKode == AbacResultat.GODKJENT;
    }

    @Deprecated
    public AbacResultat getBeslutningKode() {
        return beslutningKode();
    }

    @Deprecated
    public List<Decision> getDelbeslutninger() {
        return delbeslutninger();
    }

    @Deprecated
    public PdpRequest getPdpRequest() {
        return pdpRequest();
    }
}

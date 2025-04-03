package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;

public enum PipFagsakStatus implements RessursDataValue {
    OPPRETTET,
    UNDER_BEHANDLING;

    @Override
    public String getVerdi() {
        return name();
    }
}

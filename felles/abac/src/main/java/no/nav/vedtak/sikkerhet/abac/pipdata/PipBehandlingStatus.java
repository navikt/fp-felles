package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;

public enum PipBehandlingStatus implements RessursDataValue {
    OPPRETTET,
    UTREDES,
    FATTE_VEDTAK;

    @Override
    public String getVerdi() {
        return name();
    }
}

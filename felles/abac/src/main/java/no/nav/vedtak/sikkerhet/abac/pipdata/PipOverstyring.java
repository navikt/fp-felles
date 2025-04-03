package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;

public enum PipOverstyring implements RessursDataValue {
    OVERSTYRING;

    @Override
    public String getVerdi() {
        return name();
    }
}

package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;

public enum PipOverstyring implements RessursDataValue {
    OVERSTYRING(ForeldrepengerAttributter.VALUE_FP_AKSJONSPUNKT_OVERSTYRING);

    private final String verdi;

    PipOverstyring(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}

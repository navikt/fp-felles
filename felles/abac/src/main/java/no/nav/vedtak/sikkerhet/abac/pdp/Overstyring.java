package no.nav.vedtak.sikkerhet.abac.pdp;

import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;

public enum Overstyring implements RessursDataValue {
    OVERSTYRING(ForeldrepengerAttributter.VALUE_FP_AKSJONSPUNKT_OVERSTYRING);

    private final String verdi;

    Overstyring(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}

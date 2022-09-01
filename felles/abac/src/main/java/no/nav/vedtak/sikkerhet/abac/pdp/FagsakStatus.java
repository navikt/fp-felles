package no.nav.vedtak.sikkerhet.abac.pdp;

import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;

public enum FagsakStatus implements RessursDataValue {
    OPPRETTET(ForeldrepengerAttributter.VALUE_FP_SAK_STATUS_OPPRETTET),
    UNDER_BEHANDLING(ForeldrepengerAttributter.VALUE_FP_SAK_STATUS_BEHANDLES);

    private final String verdi;

    FagsakStatus(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}

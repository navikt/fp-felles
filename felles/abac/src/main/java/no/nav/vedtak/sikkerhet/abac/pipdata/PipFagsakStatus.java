package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;

public enum PipFagsakStatus implements RessursDataValue {
    OPPRETTET(ForeldrepengerAttributter.VALUE_FP_SAK_STATUS_OPPRETTET),
    UNDER_BEHANDLING(ForeldrepengerAttributter.VALUE_FP_SAK_STATUS_BEHANDLES);

    private final String verdi;

    PipFagsakStatus(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}

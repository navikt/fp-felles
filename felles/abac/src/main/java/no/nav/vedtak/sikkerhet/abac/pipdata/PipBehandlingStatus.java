package no.nav.vedtak.sikkerhet.abac.pipdata;

import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataValue;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;

public enum PipBehandlingStatus implements RessursDataValue {
    OPPRETTET(ForeldrepengerAttributter.VALUE_FP_BEHANDLING_STATUS_OPPRETTET),
    UTREDES(ForeldrepengerAttributter.VALUE_FP_BEHANDLING_STATUS_UTREDES),
    FATTE_VEDTAK(ForeldrepengerAttributter.VALUE_FP_BEHANDLING_STATUS_VEDTAK);

    private final String verdi;

    PipBehandlingStatus(String verdi) {
        this.verdi = verdi;
    }

    @Override
    public String getVerdi() {
        return verdi;
    }
}

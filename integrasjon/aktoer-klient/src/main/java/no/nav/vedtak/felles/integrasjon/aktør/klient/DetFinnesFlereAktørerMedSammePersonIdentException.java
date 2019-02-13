package no.nav.vedtak.felles.integrasjon.aktør.klient;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class DetFinnesFlereAktørerMedSammePersonIdentException extends IntegrasjonException {
    public DetFinnesFlereAktørerMedSammePersonIdentException(Feil feil) {
        super(feil);
    }
}

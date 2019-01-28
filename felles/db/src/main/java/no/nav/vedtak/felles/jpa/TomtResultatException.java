package no.nav.vedtak.felles.jpa;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

/**
 * Spesialisert exception som kastes når det kreves et eksakt svar fra Hibernate. Lar caller fange dette som en
 * exception.
 */
public class TomtResultatException extends TekniskException {
    public TomtResultatException(Feil feil) {
        super(feil);
    }
}

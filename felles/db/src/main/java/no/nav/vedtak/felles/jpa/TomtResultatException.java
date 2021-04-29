package no.nav.vedtak.felles.jpa;

import no.nav.vedtak.exception.TekniskException;

/**
 * Spesialisert exception som kastes når det kreves et eksakt svar fra Hibernate.
 * Lar caller fange dette som en exception.
 */
public class TomtResultatException extends TekniskException {
    public TomtResultatException(String kode, String msg) {
        super(kode, msg);
    }
}

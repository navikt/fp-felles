package no.nav.vedtak.felles.jpa;

import java.net.HttpURLConnection;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.FeilType;

/**
 * Spesialisert exception som kastes når det kreves et eksakt svar fra Hibernate.
 * Lar caller fange dette som en exception.
 */
public class TomtResultatException extends TekniskException {
    public TomtResultatException(String kode, String msg) {
        super(kode, msg);
    }

    @Override
    public int getStatusCode() {
        return HttpURLConnection.HTTP_NOT_FOUND;
    }

    @Override
    public String getFeilType() {
        return FeilType.TOMT_RESULTAT_FEIL.name();
    }
}

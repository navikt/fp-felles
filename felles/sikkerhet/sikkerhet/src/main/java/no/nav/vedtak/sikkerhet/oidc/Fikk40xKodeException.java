package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class Fikk40xKodeException extends TekniskException {

    public Fikk40xKodeException(Feil feil) {
        super(feil);
    }
}

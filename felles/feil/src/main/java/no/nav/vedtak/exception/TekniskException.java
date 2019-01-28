package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;

public class TekniskException extends VLException {

    public TekniskException(Feil feil) {
        super(feil);
    }

}

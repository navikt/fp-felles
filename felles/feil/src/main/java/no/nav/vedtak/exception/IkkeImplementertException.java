package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;

public class IkkeImplementertException extends VLException {

    public IkkeImplementertException(Feil feil) {
        super(feil);
    }

}

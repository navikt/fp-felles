package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;

public class IntegrasjonException extends VLException {

    public IntegrasjonException(Feil feil) {
        super(feil);
    }

}

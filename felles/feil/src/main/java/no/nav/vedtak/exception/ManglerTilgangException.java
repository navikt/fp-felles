package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;

public class ManglerTilgangException extends VLException {

    public ManglerTilgangException(Feil feil) {
        super(feil);
    }

}

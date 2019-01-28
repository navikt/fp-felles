package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class VlIOException extends TekniskException {

    public VlIOException(Feil feil) {
        super(feil);
    }
}

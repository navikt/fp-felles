package no.nav.vedtak.sikkerhet.abac;

import org.slf4j.Logger;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.feil.Feil;

public class PepNektetTilgangException extends ManglerTilgangException {
    public PepNektetTilgangException(Feil feil) {
        super(feil);
    }

    @Override
    public void log(Logger logger) {
        //Logg uten stacktrace, det skaper bare støy for denne typen exception
        getFeil().log(logger);
    }
}

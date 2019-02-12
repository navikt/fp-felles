package no.nav.vedtak.exception;

import no.nav.vedtak.feil.Feil;
import org.slf4j.Logger;

public abstract class VLException extends RuntimeException {

    private final Feil feil;

    VLException(Feil feil) {
        super(feil.toLogString(), feil.getCause());
        this.feil = feil;
    }

    public Feil getFeil() {
        return feil;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getFeil();
    }

    public void log(Logger logger) {
       feil.log(logger);
    }

}

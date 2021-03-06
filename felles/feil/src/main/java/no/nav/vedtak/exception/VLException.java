package no.nav.vedtak.exception;

import org.slf4j.Logger;

import no.nav.vedtak.feil.Feil;

public abstract class VLException extends RuntimeException {

    private final Feil feil;

    protected VLException(Feil feil) {
        super(feil.toLogString(), feil.getCause());
        this.feil = feil;
    }

    private Feil getFeil() {
        return feil;
    }

    public String getKode() {
        return feil.getKode();
    }

    public String getFeilmelding() {
        return feil.getFeilmelding();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getFeil();
    }

    private void log(Logger logger) {
        String text = feil.toLogString();
        switch (feil.getLogLevel()) {
            case ERROR:
                logger.error(text, this);
                break;
            case WARN:
                logger.warn(text, this);
                break;
            case INFO:
                logger.info(text);
                break;
            default:
                throw new IllegalArgumentException("Ikke-støttet LogLevel: " + feil.getLogLevel());
        }
    }

}

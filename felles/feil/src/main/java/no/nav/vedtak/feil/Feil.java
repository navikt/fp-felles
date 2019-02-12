package no.nav.vedtak.feil;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import no.nav.vedtak.exception.VLException;

public class Feil {

    private final String kode;
    private final String feilmelding;
    private final LogLevel logLevel;
    private final Class<? extends VLException> exceptionClass;
    private final Throwable cause;

    Feil(String kode, String feilmelding, LogLevel logLevel, Class<? extends VLException> exceptionClass, Throwable cause) {
        this.kode = kode;
        this.feilmelding = feilmelding;
        this.logLevel = logLevel;
        this.exceptionClass = exceptionClass;
        this.cause = cause;
    }

    public String getKode() {
        return kode;
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    public String toLogString() {
        return (kode + ":" + feilmelding).replaceAll("(\\r|\\n)", "");
    }

    @Override
    public String toString() {
        return toLogString();
    }

    public Feil log(Logger logger) {
        if (cause != null) {
            logMedCause(logger);
        } else {
            logUtenCause(logger);
        }
        return this;
    }

    public Level getLogLevel() {
        switch (logLevel) {
            case ERROR:
                return Level.ERROR;
            case WARN:
                return Level.WARN;
            case INHERIT:
                if (cause instanceof VLException) {
                    return ((VLException) cause).getFeil().getLogLevel();
                } else {
                    return Level.WARN;
                }
            case INFO:
                return Level.INFO;
            default:
                throw new IllegalArgumentException("Ukjent logLevel: " + logLevel);
        }
    }

    private void logUtenCause(Logger logger) {
        String text = toLogString();
        switch (getLogLevel()) {
            case ERROR:
                logger.error(text); // NOSONAR
                break;
            case WARN:
                logger.warn(text); // NOSONAR
                break;
            case INFO:
                logger.info(text); // NOSONAR
                break;
            case DEBUG:
                logger.debug(text); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
                break;
            case TRACE:
                logger.info(text); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
                break;
            default:
                logger.error(text); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
        }
    }

    private void logMedCause(Logger logger) {
        String text = toLogString();
        switch (getLogLevel()) {
            case ERROR:
                logger.error(text, cause); // NOSONAR
                break;
            case WARN:
                logger.warn(text, cause); // NOSONAR
                break;
            case INFO:
                logger.info(text, cause); // NOSONAR
                break;
            case DEBUG:
                logger.debug(text, cause); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
                break;
            case TRACE:
                logger.info(text, cause); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
                break;
            default:
                logger.error(text, cause); // NOSONAR
                logger.error("Ikke-støttet LogLevel: " + logLevel); // NOSONAR
        }
    }

    public VLException toException() {
        try {
            return exceptionClass.getConstructor(this.getClass()).newInstance(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new IllegalStateException("Kunne ikke gjøre feilen om til cause" + toString(), e);
        }
    }

    public Throwable getCause() {
        return cause;
    }
}

package no.nav.vedtak.feil;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import no.nav.vedtak.exception.VLException;

/**
 *
 * @deprecated Skal ikke brukes direkte
 *
 */
@Deprecated(since = "3.1.x")
public class Feil {

    private final String kode;
    private final String feilmelding;
    private final LogLevel logLevel;
    private final Class<? extends VLException> exceptionClass;
    private final Throwable cause;

    public Feil(String kode, String feilmelding, LogLevel logLevel, Class<? extends VLException> exceptionClass, Throwable cause) {
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

    private Feil log(Logger logger) {
        if (cause != null) {
            logMedCause(logger);
        } else {
            logUtenCause(logger);
        }
        return this;
    }

    private void logMedCause(Logger logger) {
        logMedCause(logger, getLogLevel());
    }

    public Level getLogLevel() {
        switch (logLevel) {
            case ERROR:
                return Level.ERROR;
            case WARN:
                return Level.WARN;
            case INHERIT:
                return Level.WARN;
            case INFO:
                return Level.INFO;
            default:
                throw new IllegalArgumentException("Ukjent logLevel: " + logLevel);
        }
    }

    private void logUtenCause(Logger logger) {
        String text = toLogString();
        switch (logLevel) {
            case ERROR:
                logger.error(text);
                break;
            case WARN:
                logger.warn(text);
                break;
            case INFO:
                logger.info(text);
                break;
            default:
                throw new IllegalArgumentException("Ikke-støttet LogLevel: " + logLevel);
        }
    }

    private void logMedCause(Logger logger, Level level) {
        String text = toLogString();
        switch (level) {
            case ERROR:
                logger.error(text, cause);
                break;
            case WARN:
                logger.warn(text, cause);
                break;
            case INFO:
                logger.info(text, cause);
                break;
            default:
                throw new IllegalArgumentException("Ikke-støttet LogLevel: " + logLevel);
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

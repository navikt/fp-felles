package no.nav.vedtak.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggerFactory for å opprette logger for spesielle formål: sporing, sikkerhet, tjeneste, etc.
 * 
 * Definerer prefix på logger definert ihht. Aura's standarder, slik at de kan konfigureres forskjellig i logback.xml og
 * tilsvarende.
 * 
 * @see <a href="https://confluence.adeo.no/display/AURA/Logging">AURA Logging</a>
 */
public final class AppLoggerFactory {
    
    private AppLoggerFactory() {
    }

    public static Logger getSporingLogger(Class<?> targetClass) {
        return getSporingLogger(targetClass.getName());
    }

    public static Logger getSporingLogger(String name) {
        return getLogger("sporing", name); //$NON-NLS-1$
    }

    public static Logger getTjenestekallLogger(Class<?> targetClass) {
        return getTjenestekallLogger(targetClass.getName());
    }

    public static Logger getTjenestekallLogger(String name) {
        return getLogger("tjenestekall", name); //$NON-NLS-1$
    }

    public static Logger getBatchLogger(Class<?> targetClass) {
        return getBatchLogger(targetClass.getName());
    }

    public static Logger getBatchLogger(String name) {
        return getLogger("batch", name); //$NON-NLS-1$
    }

    public static Logger getBatchClientLogger(Class<?> targetClass) {
        return getBatchClientLogger(targetClass.getName());
    }

    public static Logger getBatchClientLogger(String name) {
        return getLogger("batchklient", name); //$NON-NLS-1$
    }

    public static Logger getSikkerhetLogger(Class<?> targetClass) {
        return getSikkerhetLogger(targetClass.getName());
    }

    public static Logger getSikkerhetLogger(String name) {
        return getLogger("sikkerhet", name); //$NON-NLS-1$
    }

    private static Logger getLogger(String prefix, String name) {
        return LoggerFactory.getLogger(prefix + "." + name); //$NON-NLS-1$
    }
}

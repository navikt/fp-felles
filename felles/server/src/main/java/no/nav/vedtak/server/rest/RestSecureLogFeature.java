package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestSecureLogFeature implements Feature {

    // Kan evt vurdere en variabel for logger-navn. Må da registrere object/ctor framfor klasse.
    private static final Logger SECURE_LOG = LoggerFactory.getLogger("secureLogger");
    private static volatile boolean sikkerloggEnabled = false;

    public static boolean erSikkerloggEnabled() {
        return sikkerloggEnabled;
    }

    public static void sikkerloggWarning(String loggmelding) {
        if (RestSecureLogFeature.erSikkerloggEnabled()) {
            SECURE_LOG.warn(loggmelding);
        }
    }

    @Override
    public boolean configure(final FeatureContext context) {
        setSikkerloggEnabled();
        return true;
    }

    private static void setSikkerloggEnabled() {
        RestSecureLogFeature.sikkerloggEnabled = true;
    }
}

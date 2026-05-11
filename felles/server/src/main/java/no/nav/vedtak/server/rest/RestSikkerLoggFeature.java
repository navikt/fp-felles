package no.nav.vedtak.server.rest;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;


public class RestSikkerLoggFeature implements Feature {

    private static boolean sikkerloggEnabled = false;

    @Override
    public boolean configure(final FeatureContext context) {
        setSikkerloggEnabled();
        return true;
    }

    public static boolean erSikkerloggEnabled() {
        return sikkerloggEnabled;
    }

    private static void setSikkerloggEnabled() {
        RestSikkerLoggFeature.sikkerloggEnabled = true;
    }
}

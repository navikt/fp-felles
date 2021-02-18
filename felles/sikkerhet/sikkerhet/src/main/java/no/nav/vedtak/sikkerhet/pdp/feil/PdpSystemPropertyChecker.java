package no.nav.vedtak.sikkerhet.pdp.feil;

import no.nav.vedtak.util.env.Environment;

public class PdpSystemPropertyChecker {

    private static final Environment ENV = Environment.current();

    private PdpSystemPropertyChecker() {
    }

    public static String getSystemProperty(String key) {
        String p = ENV.getProperty(key);
        if (p == null) {
            throw PdpFeil.propertyManglerFeil(key);
        }
        return p;
    }
}

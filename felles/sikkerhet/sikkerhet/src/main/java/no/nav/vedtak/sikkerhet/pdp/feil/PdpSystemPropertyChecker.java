package no.nav.vedtak.sikkerhet.pdp.feil;

import static no.nav.vedtak.konfig.PropertyUtil.getProperty;

public class PdpSystemPropertyChecker {

    private PdpSystemPropertyChecker() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

    public static String getSystemProperty(String key) {
        String p = getProperty(key);
        if (p == null) {
            throw PdpFeil.FACTORY.propertyManglerFeil(key).toException();
        }
        return p;
    }
}

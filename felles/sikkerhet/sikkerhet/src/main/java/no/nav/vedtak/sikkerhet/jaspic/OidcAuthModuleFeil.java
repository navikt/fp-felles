package no.nav.vedtak.sikkerhet.jaspic;

import java.io.IOException;

import no.nav.vedtak.exception.TekniskException;

class OidcAuthModuleFeil {
    private OidcAuthModuleFeil() {

    }

    static TekniskException klarteIkkeSendeRespons(IOException e) {
        return new TekniskException("F-396795", "Klarte ikke å sende respons", e);
    }
}

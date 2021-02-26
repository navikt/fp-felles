package no.nav.vedtak.isso;

import java.io.IOException;

import no.nav.vedtak.exception.IntegrasjonException;

class SystemUserIdTokenProviderFeil {

    private SystemUserIdTokenProviderFeil() {
    }

    static IntegrasjonException klarteIkkeHenteIdTokenIOException(IOException e) {
        return new IntegrasjonException("F-116509", "Klarte ikke hente ID-token for systembrukeren", e);
    }
}

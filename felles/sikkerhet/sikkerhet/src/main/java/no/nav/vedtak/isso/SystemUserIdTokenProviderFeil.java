package no.nav.vedtak.isso;

import java.io.IOException;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.sikkerhet.oidc.VlIOException;

class SystemUserIdTokenProviderFeil {

    private SystemUserIdTokenProviderFeil() {
    }

    static IntegrasjonException klarteIkkeHenteIdTokenIOException(IOException e) {
        return new IntegrasjonException("F-116509", "Klarte ikke hente ID-token for systembrukeren", e);
    }

    static IntegrasjonException klarteIkkeHenteIdTokenVlIOException(VlIOException e) {
        return new IntegrasjonException("F-572075", "Klarte ikke hente ID-token for systembrukeren", e);
    }
}

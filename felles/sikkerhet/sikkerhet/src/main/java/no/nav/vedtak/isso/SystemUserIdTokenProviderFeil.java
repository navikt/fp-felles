package no.nav.vedtak.isso;

import java.io.IOException;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.sikkerhet.oidc.Fikk40xKodeException;
import no.nav.vedtak.sikkerhet.oidc.VlIOException;

class SystemUserIdTokenProviderFeil {

    private SystemUserIdTokenProviderFeil() {
    }

    static IntegrasjonException klarteIkkeHenteIdTokenIOException(IOException e) {
        return new IntegrasjonException("F-116509", "Klarte ikke hente ID-token for systembrukeren");
    }

    static IntegrasjonException klarteIkkeHenteIdTokenVlIOException(VlIOException e) {
        return new IntegrasjonException("F-572075", "Klarte ikke hente ID-token for systembrukeren", e);
    }

    static IntegrasjonException klarteIkkeHenteIdToken(int antall, Fikk40xKodeException e) {
        return new IntegrasjonException("F-061582", String.format("Klarte ikke hente ID-token for systembrukeren, selv etter %s forsøk", antall), e);
    }
}

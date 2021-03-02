package no.nav.vedtak.sikkerhet.jwks;

import java.net.URL;

import org.jose4j.lang.JoseException;

import no.nav.vedtak.exception.TekniskException;

class JwksFeil {

    private JwksFeil() {

    }

    static TekniskException manglerKonfigurasjonAvJwksUrl() {
        return new TekniskException("F-836283", "Mangler konfigurasjon av jwks url");
    }

    static TekniskException klarteIkkeOppdatereJwksCache(URL url) {
        return klarteIkkeOppdatereJwksCache(url, null);
    }

    static TekniskException klarteIkkeOppdatereJwksCache(URL url, Exception e) {
        return new TekniskException("F-580666", String.format("Klarte ikke oppdatere jwks cache for %s", url), e);
    }

    static TekniskException klarteIkkeParseJWKs(URL url, String jwksAsString, JoseException e) {
        return new TekniskException("F-536415", String.format("Klarte ikke parse jwks for %s, json: %s", url, jwksAsString), e);
    }
}

package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
import java.net.MalformedURLException;

import no.nav.vedtak.exception.TekniskException;

class TokenProviderFeil {

    private TokenProviderFeil() {

    }

    static TekniskException kunneIkkeHenteTokenFikkIOException(IOException e) {
        return new TekniskException("F-157385", "Kunne ikke hente token", e);
    }

    static TekniskException feilIKonfigurasjonAvOidcProvider(String key, String providerName, MalformedURLException e) {
        return new TekniskException("F-644196",
                String.format("Syntaksfeil i OIDC konfigurasjonen av '%s' for '%s'", key, providerName), e);
    }
}

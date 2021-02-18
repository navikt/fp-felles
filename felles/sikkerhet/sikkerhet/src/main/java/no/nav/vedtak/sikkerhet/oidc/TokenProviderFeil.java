package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import no.nav.vedtak.exception.TekniskException;

class TokenProviderFeil {

    private TokenProviderFeil() {

    }

    static TekniskException kunneIkkeHenteTokenFikk40xKode(int statusCode, String responseString) {
        return new TekniskException("F-922822",
                String.format("Kunne ikke hente token. Fikk http code %s og response '%s'", statusCode, responseString));
    }

    static TekniskException kunneIkkeHenteTokenFikkIOException(IOException cause) {
        return new TekniskException("F-157385", "Kunne ikke hente token");
    }

    static TekniskException kunneIkkeUrlEncodeRedirectUri(String redirectUri, UnsupportedEncodingException e) {
        return new TekniskException("F-314764",
                String.format("Could not URL-encode the redirectUri: %s", redirectUri), e);
    }

    static TekniskException fikkIkkeTokenIReponse(String tokenName) {
        return new TekniskException("F-874196",
                String.format("Fikk ikke '%s' i responsen", tokenName));
    }

    static TekniskException feilIKonfigurasjonAvOidcProvider(String key, String providerName, MalformedURLException e) {
        return new TekniskException("F-644196",
                String.format("Syntaksfeil i OIDC konfigurasjonen av '%s' for '%s'", key, providerName), e);
    }
}

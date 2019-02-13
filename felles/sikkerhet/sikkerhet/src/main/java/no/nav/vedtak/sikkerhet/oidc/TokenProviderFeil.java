package no.nav.vedtak.sikkerhet.oidc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface TokenProviderFeil extends DeklarerteFeil {

    TokenProviderFeil FACTORY = FeilFactory.create(TokenProviderFeil.class);

    @TekniskFeil(feilkode = "F-922822", feilmelding = "Kunne ikke hente token. Fikk http code %s og response '%s'", logLevel = LogLevel.WARN, exceptionClass = Fikk40xKodeException.class)
    Feil kunneIkkeHenteTokenFikk40xKode(int statusCode, String responseString);

    @TekniskFeil(feilkode = "F-157385", feilmelding = "Kunne ikke hente token.", logLevel = LogLevel.WARN, exceptionClass = VlIOException.class)
    Feil kunneIkkeHenteTokenFikkIOException(IOException cause);

    @TekniskFeil(feilkode = "F-314764", feilmelding = "Could not URL-encode the redirectUri: %s", logLevel = LogLevel.WARN)
    Feil kunneIkkeUrlEncodeRedirectUri(String redirectUri, UnsupportedEncodingException e);

    @TekniskFeil(feilkode = "F-874196", feilmelding = "Fikk ikke '%s' i responsen", logLevel = LogLevel.WARN)
    Feil fikkIkkeTokenIReponse(String tokenName);

    @TekniskFeil(feilkode = "F-644196", feilmelding = "Syntaksfeil i OIDC konfigurasjonen av '%s' for '%s'", logLevel = LogLevel.ERROR)
    Feil feilIKonfigurasjonAvOidcProvider(String key, String providerName, MalformedURLException e);
}

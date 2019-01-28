package no.nav.vedtak.sikkerhet.jwks;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import org.jose4j.lang.JoseException;

import java.net.URL;

interface JwksFeil extends DeklarerteFeil {

    JwksFeil FACTORY = FeilFactory.create(JwksFeil.class);

    @TekniskFeil(feilkode = "F-836283", feilmelding = "Mangler konfigurasjon av jwks url", logLevel = LogLevel.ERROR)
    Feil manglerKonfigurasjonAvJwksUrl();

    @TekniskFeil(feilkode = "F-192707", feilmelding = "Klarte ikke oppdatere jwks cache for %s. Http code %s", logLevel = LogLevel.ERROR)
    Feil klarteIkkeOppdatereJwksCache(URL url, int statusCode);

    @TekniskFeil(feilkode = "F-580666", feilmelding = "Klarte ikke oppdatere jwks cache for %s", logLevel = LogLevel.ERROR)
    Feil klarteIkkeOppdatereJwksCache(URL url, Exception e);

    @TekniskFeil(feilkode = "F-536415", feilmelding = "Klarte ikke parse jwks for %s, json: %s", logLevel = LogLevel.ERROR)
    Feil klarteIkkeParseJWKs(URL url, String jwksAsString, JoseException e);
}

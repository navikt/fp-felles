package no.nav.vedtak.felles.jpa;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

/**
 * Feilmeldinger knyttet til hibernate baserte spørringer, der det ønskes en strengere kontrakt iforhold til hva
 * hibernate selv returnerer.
 */
interface HibernateFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "F-108088", feilmelding = "Spørringen %s returnerte ikke et unikt resultat", logLevel = LogLevel.WARN)
    Feil ikkeUniktResultat(String spørring);

    @TekniskFeil(feilkode = "F-029343", feilmelding = "Spørringen %s returnerte mer enn eksakt ett resultat", logLevel = LogLevel.WARN)
    Feil merEnnEttResultat(String spørring);

    @TekniskFeil(feilkode = "F-650018", feilmelding = "Spørringen %s returnerte tomt resultat", logLevel = LogLevel.WARN, exceptionClass = TomtResultatException.class)
    Feil tomtResultat(String spørring);
}

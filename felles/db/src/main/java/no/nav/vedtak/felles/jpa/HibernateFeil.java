package no.nav.vedtak.felles.jpa;

import no.nav.vedtak.exception.TekniskException;

/**
 * Feilmeldinger knyttet til hibernate baserte spørringer, der det ønskes en
 * strengere kontrakt iforhold til hva hibernate selv returnerer.
 */
class HibernateFeil {
    private HibernateFeil() {

    }

    static TekniskException ikkeUniktResultat(String spørring) {
        return new TekniskException("F-108088", String.format("Spørringen %s returnerte ikke et unikt resultat", spørring));
    }

    static TekniskException merEnnEttResultat(String spørring) {
        return new TekniskException("F-029343", String.format("Spørringen %s returnerte mer enn eksakt ett resultat", spørring));

    }

    static TekniskException tomtResultat(String spørring) {
        return new TomtResultatException("F-650018", String.format("Spørringen %s returnerte tomt resultat", spørring));
    }
}

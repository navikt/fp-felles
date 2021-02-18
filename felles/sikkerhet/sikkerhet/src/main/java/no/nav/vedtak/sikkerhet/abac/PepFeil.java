package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.exception.ManglerTilgangException;

class PepFeil {

    private PepFeil() {

    }

    static ManglerTilgangException ikkeTilgangEgenAnsatt() {
        return new PepNektetTilgangException("F-788257", "Tilgangskontroll.Avslag.EgenAnsatt");
    }

    static ManglerTilgangException ikkeTilgangKode7() {
        return new PepNektetTilgangException("F-027901", "Tilgangskontroll.Avslag.Kode7");
    }

    static ManglerTilgangException ikkeTilgangKode6() {
        return new PepNektetTilgangException("F-709170", "Tilgangskontroll.Avslag.Kode6");
    }

    static ManglerTilgangException ikkeTilgang() {
        return new PepNektetTilgangException("F-608625", "Ikke tilgang");
    }
}

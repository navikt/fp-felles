package no.nav.vedtak.sikkerhet.abac;

public interface PdpKlient {

    Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest);

}

package no.nav.vedtak.sikkerhet.abac;

public interface PdpKlient {

    /**
     * Key i PdpRequest hvor token informasjon ligger.
     */
    String ENVIRONMENT_AUTH_TOKEN = "no.nav.vedtak.sikkerhet.pdp.AbacIdToken";

    Tilgangsbeslutning forespørTilgang(PdpRequest pdpRequest);

}

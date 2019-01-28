package no.nav.vedtak.sikkerhet.abac;

public interface Pep {

    Tilgangsbeslutning vurderTilgang(AbacAttributtSamling attributter);
}

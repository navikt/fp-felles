package no.nav.vedtak.sikkerhet.abac;

import java.util.List;

import no.nav.vedtak.log.sporingslogg.Sporingsdata;

public interface AbacSporingslogg {

    /** tilstandsløs - konvertere beslutning/attributter til sporingsdata. */
    List<Sporingsdata> byggSporingsdata(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter);

    void logg(List<Sporingsdata> sporingsdata);

    void loggDeny(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter);

}

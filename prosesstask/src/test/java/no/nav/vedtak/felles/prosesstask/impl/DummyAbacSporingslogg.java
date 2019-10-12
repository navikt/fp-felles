package no.nav.vedtak.felles.prosesstask.impl;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacSporingslogg;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;

@ApplicationScoped
public class DummyAbacSporingslogg implements AbacSporingslogg {

    @Override
    public List<Sporingsdata> byggSporingsdata(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
        return Collections.emptyList();
    }

    @Override
    public void logg(List<Sporingsdata> sporingsdata) {
    }

    @Override
    public void loggDeny(Tilgangsbeslutning beslutning, AbacAttributtSamling attributter) {
    }

}

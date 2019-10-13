package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.log.sporingslogg.SporingsloggId;

public interface AbacAttributtType extends SporingsloggId {

    boolean getMaskerOutput();

    boolean getValider();
    
}
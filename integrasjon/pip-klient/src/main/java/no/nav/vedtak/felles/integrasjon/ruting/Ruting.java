package no.nav.vedtak.felles.integrasjon.ruting;

import java.util.Set;

public interface Ruting {

    Set<RutingResultat> finnRutingEgenskaper(Set<String> identer);

    // Brukes for tilfelle der det med sikkerhet finnes oppgaver/aksjonspunkt. Ellers bruk identer.
    Set<RutingResultat> finnRutingEgenskaper(String saksnummer);

}

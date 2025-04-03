package no.nav.vedtak.felles.integrasjon.ruting;

import java.util.Set;

public interface Ruting {

    Set<RutingResultat> finnRutingEgenskaper(Set<String> aktørIdenter);

}

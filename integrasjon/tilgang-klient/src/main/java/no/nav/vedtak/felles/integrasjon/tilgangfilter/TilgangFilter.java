package no.nav.vedtak.felles.integrasjon.tilgangfilter;

import java.util.Set;
import java.util.UUID;

public interface TilgangFilter {

    // Returnerer hvilke saker den ansatte har tilgang til basert på oppgitte saksnummer
    Set<String> filterSaksnummer(UUID ansattOid, Set<String> saksnummer);

    // Returnerer hvilke identer den ansatte har tilgang til basert på oppgitte identer
    Set<String> filterIdenter(UUID ansattOid, Set<String> identer);

}

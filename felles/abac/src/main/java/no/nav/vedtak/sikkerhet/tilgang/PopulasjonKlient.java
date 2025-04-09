package no.nav.vedtak.sikkerhet.tilgang;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;

public interface PopulasjonKlient {

    Tilgangsvurdering vurderTilgangInternBruker(UUID ansattOid, Set<String> personIdenter, Set<String> aktørIdenter, String saksnummer);
    Tilgangsvurdering vurderTilgangEksternBruker(String subjectPersonIdent, Set<String> personIdenter, Set<String> aktørIdenter, int aldersgrense);

}

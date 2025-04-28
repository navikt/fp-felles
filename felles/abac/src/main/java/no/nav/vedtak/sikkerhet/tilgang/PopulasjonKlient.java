package no.nav.vedtak.sikkerhet.tilgang;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.abac.policy.Tilgangsvurdering;

public interface PopulasjonKlient {

    Tilgangsvurdering vurderTilgangInternBruker(UUID ansattOid, Set<String> identer, String saksnummer, UUID behandling);
    Tilgangsvurdering vurderTilgangEksternBruker(String subjectPersonIdent, Set<String> identer, int aldersgrense);

}

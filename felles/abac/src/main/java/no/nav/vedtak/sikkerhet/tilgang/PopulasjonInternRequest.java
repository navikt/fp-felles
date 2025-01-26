package no.nav.vedtak.sikkerhet.tilgang;

import java.util.Set;
import java.util.UUID;

public record PopulasjonInternRequest(UUID ansattOid,
                                      Set<String> personIdenter,
                                      Set<String> aktørIdenter) {

}

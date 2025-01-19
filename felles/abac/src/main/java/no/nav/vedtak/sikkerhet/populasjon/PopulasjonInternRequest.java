package no.nav.vedtak.sikkerhet.populasjon;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

public record PopulasjonInternRequest(UUID ansattOid,
                                      Set<AnsattGruppe> kreverGrupper,
                                      Set<String> personIdenter,
                                      Set<String> aktørIdenter) {

}

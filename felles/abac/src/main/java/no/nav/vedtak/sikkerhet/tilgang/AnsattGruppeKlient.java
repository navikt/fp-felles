package no.nav.vedtak.sikkerhet.tilgang;

import java.util.Set;
import java.util.UUID;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

public interface AnsattGruppeKlient {

    Set<AnsattGruppe> vurderAnsattGrupper(UUID ansattOid, Set<AnsattGruppe> påkrevdeGrupper);

}

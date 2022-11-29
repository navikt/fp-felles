package no.nav.vedtak.sikkerhet.abac.pipdata;

import javax.validation.Valid;
import java.util.Set;

public record AbacPipDto(@Valid Set<PipAktørId> aktørIder, PipFagsakStatus fagsakStatus, PipBehandlingStatus behandlingStatus) {
}

package no.nav.vedtak.sikkerhet.abac.pipdata;

import java.util.Set;

import javax.validation.Valid;

public record AbacPipDto(@Valid Set<PipAktørId> aktørIder, PipFagsakStatus fagsakStatus, PipBehandlingStatus behandlingStatus) {
}

package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import jakarta.validation.constraints.NotNull;

public record HentDokumentQuery(@NotNull String journalpostId, @NotNull String dokumentId) {
}

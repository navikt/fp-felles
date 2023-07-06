package no.nav.vedtak.felles.integrasjon.saf;

import jakarta.validation.constraints.NotNull;

public record HentDokumentQuery(@NotNull String journalpostId, @NotNull String dokumentId, @NotNull String variantFormat) {
}

package no.nav.vedtak.felles.integrasjon.saf;

import javax.validation.constraints.NotNull;

public record HentDokumentQuery(@NotNull String journalpostId, @NotNull String dokumentId,
                                @NotNull String variantFormat) {
}

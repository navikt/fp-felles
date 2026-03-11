package no.nav.vedtak.feil;

import jakarta.validation.constraints.NotNull;

public record FeilDto(@NotNull String feiltype, @NotNull String feilmelding) {

    public FeilDto {
        if (feiltype == null || feilmelding == null) {
            throw new IllegalArgumentException("Type og feilmelding kan ikke være null");
        }
    }

    public FeilDto(FeilType feiltype, String feilmelding) {
        this(feiltype.name(), feilmelding);
    }

}

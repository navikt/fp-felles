package no.nav.vedtak.feil;

import jakarta.validation.constraints.NotNull;

public record FeltFeilDto(@NotNull String navn, @NotNull String melding, String metainformasjon) {

    public FeltFeilDto {
        if (navn == null || melding == null) {
            throw new IllegalArgumentException("Navn og melding kan ikke være null");
        }
    }

    public FeltFeilDto(String navn, String melding) {
        this(navn, melding, null);
    }
}

package no.nav.vedtak.feil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotNull;

public record FeilDto(@NotNull String type, @NotNull String feilmelding, Collection<FeltFeilDto> feltFeil) {

    public FeilDto {
        if (type == null || feilmelding == null) {
            throw new IllegalArgumentException("Type og feilmelding kan ikke være null");
        }
    }

    public FeilDto(FeilType type, String feilmelding) {
        this(type.name(), feilmelding, List.of());
    }

    public FeilDto(FeilType type, String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this(type.name(), feilmelding, feltFeil);
    }

    public Collection<FeltFeilDto> safeFeltFeil() {
        return Optional.ofNullable(feltFeil).orElseGet(List::of);
    }

}

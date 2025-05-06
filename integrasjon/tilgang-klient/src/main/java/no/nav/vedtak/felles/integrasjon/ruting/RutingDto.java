package no.nav.vedtak.felles.integrasjon.ruting;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class RutingDto {

    private RutingDto() {
    }

    // For å sjekke rutingegenskaper basert på identer
    public record IdenterRequest(@Valid Set<String> identer) { }

    // For å sjekke rutingegenskaper basert på saksnummer
    public record SakRequest(@Valid @NotNull String saksnummer) { }

    // Rutingegenkaper for identer eller sak
    public record Respons(Set<RutingResultat> resultater) { }
}

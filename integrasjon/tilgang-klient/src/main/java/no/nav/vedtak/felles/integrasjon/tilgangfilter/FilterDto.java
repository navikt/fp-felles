package no.nav.vedtak.felles.integrasjon.tilgangfilter;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class FilterDto {

    private FilterDto() {
    }

    // For å sjekke hvilke saker den ansatte har tilgang til basert på saksnummer
    public record SaksnummerRequest(@NotNull UUID ansattOid, Set<String> saker) { }

    // For å sjekke hvilke identer den ansatte har tilgang til basert på identer
    public record IdenterRequest(@NotNull UUID ansattOid, Set<String> identer) { }

    // Hvilke av sakene/identene i request som den ansatte har tilgang til
    public record Respons(Set<String> harTilgang) { }
}

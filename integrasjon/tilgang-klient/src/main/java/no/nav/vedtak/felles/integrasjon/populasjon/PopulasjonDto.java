package no.nav.vedtak.felles.integrasjon.populasjon;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class PopulasjonDto {

    private PopulasjonDto() {
    }

    // For å om den ansatte har tilgang til personer basert på identer, saksnummer og behandling
    public record InternRequest(@NotNull UUID ansattOid,
                                @NotNull @Valid Set<String> identer,
                                @Valid String saksnummer,
                                @Valid UUID behandling) { }

    // For å om innlogget borger har tilgang til å lese/endre basert på identer og alder
    public record EksternRequest(@NotNull String subjectPersonIdent,
                                 @NotNull @Valid Set<String> identer,
                                 @Valid int aldersgrense) { }

    // Godkjent eller avslått tilgang med evt avslagsårsak. Dessuten ident til bruk i auditlogging
    public record Respons(@NotNull PopulasjonTilgangResultat tilgangResultat,
                          String årsak,
                          String auditIdent) { }
}

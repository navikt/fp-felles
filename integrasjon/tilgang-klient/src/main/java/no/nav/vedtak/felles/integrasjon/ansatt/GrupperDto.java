package no.nav.vedtak.felles.integrasjon.ansatt;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

public class GrupperDto {

    private GrupperDto() {
    }

    // Request får å hente alle grupper den ansatte er medlem av
    public record MedlemRequest(@NotNull @Valid UUID ansattOid) { }

    // Request får å sjekke om angitt ansatt er medlem av angitte grupper
    public record FilterRequest(@NotNull @Valid UUID ansattOid, @NotNull @Valid Set<AnsattGruppe> grupper) { }

    // Respons med hvilke grupper den ansatte er medlem av
    public record Respons(@NotNull Set<AnsattGruppe> grupper) { }

}

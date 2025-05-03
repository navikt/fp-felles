package no.nav.vedtak.felles.integrasjon.sak;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record InvaliderSakRequest(@NotNull @Valid String saksnummer) {
}

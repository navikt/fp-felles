package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

record OrganisasjonDetaljer(LocalDateTime registreringsdato, LocalDate opphoersdato, List<AdresseEReg> forretningsadresser,
        List<AdresseEReg> postadresser) {


    List<AdresseEReg> getForretningsadresser() {
        return Optional.ofNullable(forretningsadresser()).orElseGet(() -> List.of());
    }

    List<AdresseEReg> getPostadresser() {
        return Optional.ofNullable(postadresser()).orElseGet(() -> List.of());
    }
}
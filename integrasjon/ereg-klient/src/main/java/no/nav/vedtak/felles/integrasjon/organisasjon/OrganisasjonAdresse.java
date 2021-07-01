package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record OrganisasjonAdresse(String organisasjonsnummer, OrganisasjonstypeEReg type, Navn navn,
        OrganisasjonDetaljer organisasjonDetaljer) {


    public String getNavn() {
        return Optional.ofNullable(navn)
                .map(Navn::getNavn)
                .orElse(null);
    }

    public AdresseEReg getKorrespondanseadresse() {
        return !getPostadresser().isEmpty() ? getPostadresser().get(0) : getForretningsadresser().get(0);
    }

    public List<AdresseEReg> getForretningsadresser() {
        return Optional.ofNullable(organisasjonDetaljer)
                .map(OrganisasjonDetaljer::getForretningsadresser)
                .filter(Objects::nonNull)
                .orElseGet(() -> List.of());
    }

    public List<AdresseEReg> getPostadresser() {
        return Optional.ofNullable(organisasjonDetaljer)
                .map(OrganisasjonDetaljer::getPostadresser)
                .filter(Objects::nonNull)
                .orElseGet(() -> List.of());
    }

    public LocalDate getRegistreringsdato() {
        return Optional.ofNullable(organisasjonDetaljer)
                .map(OrganisasjonDetaljer::registreringsdato)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    public LocalDate getOpphørsdato() {
        return Optional.ofNullable(organisasjonDetaljer)
                .map(OrganisasjonDetaljer::opphoersdato)
                .filter(Objects::nonNull)
                .orElse(null);
    }


    private static record OrganisasjonDetaljer(LocalDateTime registreringsdato, LocalDate opphoersdato,
            List<AdresseEReg> forretningsadresser, List<AdresseEReg> postadresser) {

        private List<AdresseEReg> getForretningsadresser() {
            return forretningsadresser != null ? forretningsadresser : List.of();
        }

        private List<AdresseEReg> getPostadresser() {
            return postadresser != null ? postadresser : List.of();
        }
    }

}

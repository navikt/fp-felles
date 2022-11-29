package no.nav.vedtak.felles.integrasjon.organisasjon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrganisasjonEReg(String organisasjonsnummer,
                               OrganisasjonstypeEReg type,
                               Navn navn,
                               OrganisasjonDetaljer organisasjonDetaljer,
                               VirksomhetDetaljer virksomhetDetaljer) {


    public String getNavn() {
        return Optional.ofNullable(navn())
                .map(Navn::getNavn)
                .orElse(null);
    }

    public LocalDate getRegistreringsdato() {
        return Optional.ofNullable(organisasjonDetaljer())
                .map(OrganisasjonDetaljer::registreringsdato)
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }

    public LocalDate getOpphørsdato() {
        return Optional.ofNullable(organisasjonDetaljer())
                .map(OrganisasjonDetaljer::opphoersdato)
                .orElse(null);
    }

    public LocalDate getOppstartsdato() {
        return Optional.ofNullable(virksomhetDetaljer())
                .map(VirksomhetDetaljer::oppstartsdato)
                .orElse(null);
    }

    public LocalDate getNedleggelsesdato() {
        return Optional.ofNullable(virksomhetDetaljer())
                .map(VirksomhetDetaljer::nedleggelsesdato)
                .orElse(null);
    }

    private record Navn(String navnelinje1, String navnelinje2, String navnelinje3, String navnelinje4, String navnelinje5) {

        private String getNavn() {
            return Stream.of(navnelinje1(), navnelinje2(), navnelinje3(), navnelinje4(), navnelinje5())
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(Predicate.not(String::isEmpty))
                    .reduce("", (a, b) -> a + " " + b).trim();
        }
    }

    private record OrganisasjonDetaljer(LocalDateTime registreringsdato, LocalDate opphoersdato) {

    }

    private record VirksomhetDetaljer(LocalDate oppstartsdato, LocalDate nedleggelsesdato) {
    }

}

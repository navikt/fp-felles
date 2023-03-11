package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.vedtak.konfig.Tid;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JuridiskEnhetVirksomheter(String organisasjonsnummer, OrganisasjonstypeEReg type, OrganisasjonDetaljer organisasjonDetaljer,
                                        List<DriverVirksomhet> driverVirksomheter) {


    public List<String> getEksaktVirksomhetForDato(LocalDate hentedato) {
        if (!OrganisasjonstypeEReg.JURIDISK_ENHET.equals(type) || getOpphørsdatoNonNull().isBefore(hentedato)) {
            return List.of();
        }
        return Optional.ofNullable(driverVirksomheter)
            .orElse(List.of())
            .stream()
            .filter(v -> v.gyldighetsperiode().fom().isBefore(hentedato) && v.gyldighetsperiode().getTomNonNull().isAfter(hentedato))
            .map(DriverVirksomhet::organisasjonsnummer)
            .toList();
    }

    public LocalDate getRegistreringsdato() {
        return Optional.ofNullable(organisasjonDetaljer).map(OrganisasjonDetaljer::registreringsdato).map(LocalDateTime::toLocalDate).orElse(null);
    }

    public LocalDate getOpphørsdato() {
        return Optional.ofNullable(organisasjonDetaljer).map(OrganisasjonDetaljer::opphoersdato).orElse(null);
    }

    private LocalDate getOpphørsdatoNonNull() {
        return Optional.ofNullable(organisasjonDetaljer).map(OrganisasjonDetaljer::opphoersdato).orElse(Tid.TIDENES_ENDE);
    }

    private record OrganisasjonDetaljer(LocalDateTime registreringsdato, LocalDate opphoersdato) {
    }

    private record DriverVirksomhet(String organisasjonsnummer, Periode gyldighetsperiode) {
    }

    private record Periode(LocalDate fom, LocalDate tom) {

        public LocalDate getTomNonNull() {
            return Optional.ofNullable(tom).orElse(Tid.TIDENES_ENDE);
        }

    }

}


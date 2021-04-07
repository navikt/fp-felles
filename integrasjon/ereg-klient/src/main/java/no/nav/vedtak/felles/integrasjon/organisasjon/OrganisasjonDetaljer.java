package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

class OrganisasjonDetaljer {

    @JsonProperty("registreringsdato")
    private LocalDateTime registreringsdato;
    @JsonProperty("opphoersdato")
    private LocalDate opphoersdato;
    @JsonProperty("forretningsadresser")
    private List<AdresseEReg> forretningsadresser;
    @JsonProperty("postadresser")
    private List<AdresseEReg> postadresser;

    LocalDateTime getRegistreringsdato() {
        return registreringsdato;
    }

    LocalDate getOpphoersdato() {
        return opphoersdato;
    }

    List<AdresseEReg> getForretningsadresser() {
        return forretningsadresser != null ? forretningsadresser : Collections.emptyList();
    }

    List<AdresseEReg> getPostadresser() {
        return postadresser != null ? postadresser : Collections.emptyList();
    }
}
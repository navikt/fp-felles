package no.nav.vedtak.felles.integrasjon.organisasjon.hent;

import java.time.LocalDate;

public class HentOrganisasjonForJuridiskRequest {
    private String orgnummer;
    private LocalDate henteForDato;

    public HentOrganisasjonForJuridiskRequest(String orgnummer, LocalDate henteForDato) {
        this.orgnummer = orgnummer;
        this.henteForDato = henteForDato;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public LocalDate getHenteForDato() {
        return henteForDato;
    }
}

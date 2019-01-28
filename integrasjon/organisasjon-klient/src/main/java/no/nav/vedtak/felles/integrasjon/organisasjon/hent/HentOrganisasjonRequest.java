package no.nav.vedtak.felles.integrasjon.organisasjon.hent;

public class HentOrganisasjonRequest {
    private String orgnummer;

    public HentOrganisasjonRequest(String orgnummer) {
        this.orgnummer = orgnummer;
    }

    public String getOrgnummer() {
        return orgnummer;
    }
}

package no.nav.foreldrepenger.integrasjon.organisasjon.hent;

public class HentOrganisasjonRequest {
    private String orgnummer;
    private boolean medAntallAnsatte;

    public HentOrganisasjonRequest(String orgnummer) {
        this.orgnummer = orgnummer;
    }

    public HentOrganisasjonRequest(String orgnummer, boolean medAntallAnsatte) {
        this.orgnummer = orgnummer;
        this.medAntallAnsatte = medAntallAnsatte;
    }

    public String getOrgnummer() {
        return orgnummer;
    }

    public boolean getMedAntallAnsatte() {
        return medAntallAnsatte;
    }
}

package no.nav.vedtak.felles.integrasjon.organisasjon;

public record AdresseEReg(String adresselinje1,
        String adresselinje2,
        String adresselinje3,
        String kommunenummer,
        String landkode,
        String postnummer,
        String poststed) {
}

package no.nav.vedtak.felles.integrasjon.organisasjon;

public interface OrgInfo {

    OrganisasjonEReg hentOrganisasjon(String orgnummer);

    OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer);

}

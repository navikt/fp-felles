package no.nav.vedtak.felles.integrasjon.organisasjon;

public interface OrgInfo {

    OrganisasjonEReg hentOrganisasjon(String orgnummer);

    <T> T hentOrganisasjon(String orgnummer, Class<T> clazz);

    String hentOrganisasjonNavn(String orgnummer);

    JuridiskEnhetVirksomheter hentOrganisasjonHistorikk(String orgnummer);

}

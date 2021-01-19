package no.nav.vedtak.felles.integrasjon.organisasjon;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Alternative
public class OrganisasjonJerseyRestKlient extends AbstractJerseyOidcRestClient {

    private static final String ENDPOINT_KEY = "organisasjon.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/ereg/api/v1/organisasjon";

    private URI endpoint;

    public OrganisasjonJerseyRestKlient() {
    }

    @Inject
    public OrganisasjonJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        return get(orgnummer, OrganisasjonEReg.class);
    }

    public OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer) {
        return get(orgnummer, OrganisasjonAdresse.class);
    }

    private <T> T get(String orgnummer, Class<T> clazz) {
        return client.target(endpoint)
                .path(orgnummer)
                .request(APPLICATION_JSON_TYPE)
                .get(clazz);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}

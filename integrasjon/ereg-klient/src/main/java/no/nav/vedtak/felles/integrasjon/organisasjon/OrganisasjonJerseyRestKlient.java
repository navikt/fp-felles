package no.nav.vedtak.felles.integrasjon.organisasjon;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class OrganisasjonJerseyRestKlient extends AbstractJerseyOidcRestClient implements OrgInfo {

    private static final Logger LOG = LoggerFactory.getLogger(OrganisasjonJerseyRestKlient.class);
    private static final String ENDPOINT_KEY = "organisasjon.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/ereg/api/v1/organisasjon";

    private URI endpoint;

    public OrganisasjonJerseyRestKlient() {
    }

    @Inject
    public OrganisasjonJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        return get(orgnummer, OrganisasjonEReg.class);
    }

    @Override
    public OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer) {
        return get(orgnummer, OrganisasjonAdresse.class);
    }

    private <T> T get(String orgnummer, Class<T> clazz) {
        try {
            var target = client.target(endpoint).path(orgnummer);
            LOG.warn("Henter organisasjoninfo for {} fra {}", orgnummer, target.getUri());
            return target
                    .path(orgnummer)
                    .request(APPLICATION_JSON_TYPE)
                    .get(clazz);
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente organisasjoninfo", e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}

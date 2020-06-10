package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;


@ApplicationScoped
public class OrganisasjonRestKlient {

    private static final String ENDPOINT_KEY = "organisasjon.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/ereg/api/v1/organisasjon";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public OrganisasjonRestKlient() {
    }

    @Inject
    public OrganisasjonRestKlient(OidcRestClient oidcRestClient,
                                  @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        var request = URI.create(endpoint.toString() + "/" + orgnummer);
        return oidcRestClient.get(request, OrganisasjonEReg.class);
    }

    public OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer) {
        var request = URI.create(endpoint.toString() + "/" + orgnummer);
        return oidcRestClient.get(request, OrganisasjonAdresse.class);
    }

}

package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestCompact;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.CONTEXT, endpointProperty = "organisasjon.rs.url",
    endpointDefault = "https://modapp.adeo.no/ereg/api/v1/organisasjon")
@ApplicationScoped
public class OrganisasjonNativeRestKlient implements OrgInfo {

    private RestCompact restKlient;
    private URI endpoint;

    OrganisasjonNativeRestKlient() {
        // CDI proxyable
    }

    @Inject
    public OrganisasjonNativeRestKlient(RestCompact restKlient) {
        this.restKlient = restKlient;
        this.endpoint = RestConfig.endpointFromAnnotation(OrganisasjonNativeRestKlient.class);
    }

    @Override
    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        var uri = lagURI(orgnummer);
        return restKlient.contextGetValue(uri, OrganisasjonEReg.class);
    }

    @Override
    public OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer) {
        var uri = lagURI(orgnummer);
        return restKlient.contextGetValue(uri, OrganisasjonAdresse.class);
    }

    private URI lagURI(String orgnummer) {
        return UriBuilder.fromUri(endpoint).path(orgnummer).build();
    }

}

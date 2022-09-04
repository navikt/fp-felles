package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.net.URI;
import java.net.http.HttpRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.NativeKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestKlient;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;

@NativeKlient
@ApplicationScoped
public class OrganisasjonNativeRestKlient implements OrgInfo {

    private static final String ENDPOINT_KEY = "organisasjon.rs.url";
    private static final String DEFAULT_URI = "https://modapp.adeo.no/ereg/api/v1/organisasjon";

    private RestKlient restKlient;
    private URI endpoint;

    OrganisasjonNativeRestKlient() {
        // CDI proxyable
    }

    @Inject
    public OrganisasjonNativeRestKlient(RestKlient restKlient,
                                        @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.restKlient = restKlient;
        this.endpoint = endpoint;
    }

    @Override
    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        var request = lagRequest(orgnummer);
        return restKlient.send(request, OrganisasjonEReg.class);
    }

    @Override
    public OrganisasjonAdresse hentOrganisasjonAdresse(String orgnummer) {
        var request = lagRequest(orgnummer);
        return restKlient.send(request, OrganisasjonAdresse.class);
    }

    private HttpRequest lagRequest(String orgnummer) {
        var path = UriBuilder.fromUri(endpoint).path(orgnummer).build();
        return restKlient.request().builder(SikkerhetContext.BRUKER)
            .uri(path)
            .GET()
            .build();
    }

}

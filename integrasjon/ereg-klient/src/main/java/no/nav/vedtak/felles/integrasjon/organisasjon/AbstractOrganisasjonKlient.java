package no.nav.vedtak.felles.integrasjon.organisasjon;

import java.net.URI;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

// Extend og annoter
//@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "organisasjon.rs.url",
//  endpointDefault = "https://ereg-services.intern.nav.no/api/v2/organisasjon")
public abstract class AbstractOrganisasjonKlient implements OrgInfo {

    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected AbstractOrganisasjonKlient() {
        this(RestClient.client());
    }

    protected AbstractOrganisasjonKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    @Override
    public OrganisasjonEReg hentOrganisasjon(String orgnummer) {
        return hentOrganisasjon(orgnummer, OrganisasjonEReg.class);
    }

    @Override
    public <T> T hentOrganisasjon(String orgnummer, Class<T> clazz) {
        var uri = lagURI(orgnummer);
        var request = RestRequest.newGET(uri, restConfig);
        return restKlient.send(request, clazz);
    }

    @Override
    public String hentOrganisasjonNavn(String orgnummer) {
        return hentOrganisasjon(orgnummer, OrganisasjonEReg.class).getNavn();
    }

    @Override
    @Deprecated /* Bruk hentUtvidetOrganisasjon */
    public JuridiskEnhetVirksomheter hentOrganisasjonHistorikk(String orgnummer) {
        return hentUtvidetOrganisasjon(orgnummer, JuridiskEnhetVirksomheter.class);
    }

    @Override
    public UtvidetOrganisasjonEReg hentUtvidetOrganisasjon(String orgnummer) {
        return hentUtvidetOrganisasjon(orgnummer, UtvidetOrganisasjonEReg.class);
    }

    @Override
    public <T> T hentUtvidetOrganisasjon(String orgnummer, Class<T> clazz) {
        var query = UriBuilder.fromUri(restConfig.endpoint())
            .path(orgnummer)
            .queryParam("inkluderHierarki", true)
            .queryParam("inkluderHistorikk", true)
            .build();
        var request = RestRequest.newGET(query, restConfig);
        return restKlient.send(request, clazz);
    }

    private URI lagURI(String orgnummer) {
        return UriBuilder.fromUri(restConfig.endpoint()).path(orgnummer).build();
    }

    protected RestClient getRestKlient() {
        return restKlient;
    }

}

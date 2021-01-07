package no.nav.vedtak.felles.integrasjon.sak.v1;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

// @ApplicationScoped
public class SakJerseyRestKlient extends AbstractJerseyOidcRestClient implements SakClient {

    private static final String ENDPOINT_KEY = "sak.rs.url";
    private static final String DEFAULT_URI = "http://sak.default/api/v1/saker";

    private URI endpoint;

    public SakJerseyRestKlient() {
    }

    @Inject
    public SakJerseyRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this(endpoint, new ClientRequestFilter[0]);
    }

    SakJerseyRestKlient(URI endpoint, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
    }

    @Override
    public SakJson opprettSak(SakJson sak) {
        return client.target(endpoint)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(sak))
                .invoke(SakJson.class);
    }

    @Override
    public Optional<SakJson> finnForSaksnummer(String saksnummer) throws Exception {
        return Arrays.stream(client.target(endpoint)
                .queryParam("fagsakNr", saksnummer)
                .request(APPLICATION_JSON_TYPE)
                .get(SakJson[].class))
                .findFirst();
    }

    @Override
    public SakJson hentSakId(String sakId) {
        return client.target(endpoint)
                .path(sakId)
                .request(APPLICATION_JSON_TYPE)
                .get(SakJson.class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}

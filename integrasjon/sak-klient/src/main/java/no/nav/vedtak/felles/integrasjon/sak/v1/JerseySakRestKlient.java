package no.nav.vedtak.felles.integrasjon.sak.v1;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class JerseySakRestKlient extends AbstractJerseyOidcRestClient implements SakClient {

    static final String FAGSAK_NR = "fagsakNr";
    private static final String ENDPOINT_KEY = "sak.rs.url";
    private static final String DEFAULT_URI = "http://sak.default/api/v1/saker";

    private URI endpoint;

    public JerseySakRestKlient() {
    }

    @Inject
    public JerseySakRestKlient(@KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this(endpoint, new ClientRequestFilter[0]);
    }

    JerseySakRestKlient(String endpoint, ClientRequestFilter... filters) {
        this(URI.create(endpoint), filters);
    }

    JerseySakRestKlient(URI endpoint, ClientRequestFilter... filters) {
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
                .queryParam(FAGSAK_NR, saksnummer)
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

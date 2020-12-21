package no.nav.vedtak.felles.integrasjon.sak.v1;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOIDCRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

// @ApplicationScoped
public class SakJerseyRestKlient extends AbstractJerseyOIDCRestClient {

    private static final String ENDPOINT_KEY = "sak.rs.url";
    private static final String DEFAULT_URI = "http://sak.default/api/v1/saker";

    private URI endpoint;

    public SakJerseyRestKlient() {
    }

    @Inject
    public SakJerseyRestKlient(
            @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    public SakJson opprettSak(SakJson.Builder request) {
        return client.target(endpoint)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(request.build()))
                .invoke(SakJson.class);
    }

    public Optional<SakJson> finnForSaksnummer(String saksnummer) throws Exception {
        return Arrays.stream(client.target(endpoint)
                .queryParam("fagsakNr", saksnummer)
                .request(APPLICATION_JSON_TYPE)
                .get(SakJson[].class))
                .findFirst();
    }

    public SakJson hentSakId(String sakId) {
        return client.target(endpoint)
                .path(sakId)
                .request(APPLICATION_JSON_TYPE)
                .get(SakJson.class);
    }

}

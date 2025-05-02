package no.nav.vedtak.felles.integrasjon.sak;

import java.net.URI;

import jakarta.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;


public abstract class AbstractInvaliderSakKlient {

    private final RestClient restClient;
    private final RestConfig restConfig;
    private final URI invaliderUri;

    protected AbstractInvaliderSakKlient() {
        this.restClient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.invaliderUri = UriBuilder.fromUri(restConfig.fpContextPath()).path("/api/populasjon/invalidersak").build();
    }

    protected void invaliderSak(String saksnummer) {
        var payload = new InvaliderSakRequest(saksnummer);
        var request = RestRequest.newPOSTJson(payload, invaliderUri, restConfig);
        restClient.sendReturnOptional(request, String.class);
    }

}

package no.nav.vedtak.felles.integrasjon.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class RestSupportKlientProdusent {

    @Produces
    public RestKlient getRestKlient() {
        return RestKlient.client();
    }

    @Produces
    public RestRequest getRestRequest() {
        return RestRequest.request();
    }

}

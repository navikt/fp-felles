package no.nav.vedtak.felles.integrasjon.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import no.nav.vedtak.klient.http.DefaultHttpClient;

@ApplicationScoped
public class RestSupportClientProducer {

    @Produces
    public RestClient getRestClient() {
        return new RestClient();
    }

    @Produces
    public DefaultHttpClient getHttpKlient() {
        return DefaultHttpClient.client();
    }

}

package no.nav.vedtak.felles.integrasjon.pdl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.klient.http.DefaultHttpClient;

@ApplicationScoped
public class RestSupportClientProducer {

    @Produces
    public Persondata getPdlClient() {
        return new PdlKlient();
    }

    @Produces
    public RestClient getRestClient() {
        return RestClient.client();
    }

    @Produces
    public DefaultHttpClient getHttpKlient() {
        return DefaultHttpClient.client();
    }

}

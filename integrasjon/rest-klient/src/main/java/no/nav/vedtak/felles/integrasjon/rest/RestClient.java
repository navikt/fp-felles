package no.nav.vedtak.felles.integrasjon.rest;

import no.nav.vedtak.klient.http.DefaultHttpClient;

public final class RestClient extends BaseRestClient {

    private RestClient() {
        super(DefaultHttpClient.client());
    }

    public static RestClient client() {
        return new RestClient();
    }
}

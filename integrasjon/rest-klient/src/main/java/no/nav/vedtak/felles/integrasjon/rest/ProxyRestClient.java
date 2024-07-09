package no.nav.vedtak.felles.integrasjon.rest;

import no.nav.vedtak.klient.http.ProxyHttpClient;

public final class ProxyRestClient extends BaseRestClient {

    private ProxyRestClient() {
        super(ProxyHttpClient.client());
    }

    public static ProxyRestClient client() {
        return new ProxyRestClient();
    }

}

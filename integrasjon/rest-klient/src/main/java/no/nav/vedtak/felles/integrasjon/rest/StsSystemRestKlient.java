package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;

import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 *
 */
public class StsSystemRestKlient extends AbstractOidcRestClient {

    public StsSystemRestKlient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    String getOIDCToken() {
        return TokenProvider.getStsSystemToken().token();
    }

}

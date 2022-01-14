package no.nav.vedtak.felles.integrasjon.rest;

import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.tokenhenter.StsAccessTokenKlient;

/**
 * Klassen legger dynamisk på headere for å propagere sikkerhetskonteks og callId
 *
 */
public class StsSystemRestKlient extends AbstractOidcRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(StsSystemRestKlient.class);

    public StsSystemRestKlient(CloseableHttpClient client) {
        super(client);
    }

    @Override
    String getOIDCToken() {
        return StsAccessTokenKlient.hentAccessToken();
    }

}

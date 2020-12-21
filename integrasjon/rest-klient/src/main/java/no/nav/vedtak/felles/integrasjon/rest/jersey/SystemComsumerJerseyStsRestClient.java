package no.nav.vedtak.felles.integrasjon.rest.jersey;

import no.nav.vedtak.felles.integrasjon.rest.StsAccessTokenClient;

/*
 * Tilpasset DKIF sin on-behalf-of med systembruker og STS
 */
public class SystemComsumerJerseyStsRestClient extends AbstractJerseyRestClient {

    public SystemComsumerJerseyStsRestClient(StsAccessTokenClient client) {
        super(new StandardHeadersRequestFilter(), new StsAccessTokenClientRequestFilter(client));
    }
}

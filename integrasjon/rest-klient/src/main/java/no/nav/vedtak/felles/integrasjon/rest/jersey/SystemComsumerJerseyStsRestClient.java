package no.nav.vedtak.felles.integrasjon.rest.jersey;

/*
 * Tilpasset DKIF sin on-behalf-of med systembruker og STS
 */
public class SystemComsumerJerseyStsRestClient extends AbstractJerseyRestClient {

    public SystemComsumerJerseyStsRestClient(StsAccessTokenJerseyClient client) {
        super(new StandardHeadersRequestFilter(), new StsAccessTokenClientRequestFilter(client));
    }
}

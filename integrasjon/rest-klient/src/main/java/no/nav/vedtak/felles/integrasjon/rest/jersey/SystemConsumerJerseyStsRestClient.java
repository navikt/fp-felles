package no.nav.vedtak.felles.integrasjon.rest.jersey;

/*
 * Tilpasset DKIF sin on-behalf-of med systembruker og STS
 */
public class SystemConsumerJerseyStsRestClient extends AbstractJerseyRestClient {

    public SystemConsumerJerseyStsRestClient(StsAccessTokenJerseyClient client) {
        super(new StsAccessTokenClientRequestFilter(client));
    }
}

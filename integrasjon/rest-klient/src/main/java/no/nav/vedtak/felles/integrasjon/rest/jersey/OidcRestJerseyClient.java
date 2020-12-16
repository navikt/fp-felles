package no.nav.vedtak.felles.integrasjon.rest.jersey;

public class OidcRestJerseyClient extends AbstractJerseyRestClient {
    public OidcRestJerseyClient() {
        super(OIDCTokenRequestFilter.class);
    }
}

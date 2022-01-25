package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 *
 * Dette filteret setter system token i 2 headere.
 *
 */
public class SystemTokenClientRequestFilter implements ClientRequestFilter {

    private final String tema;
    private final StsAccessTokenProvider stsProvider;

    public SystemTokenClientRequestFilter(StsAccessTokenProvider provider, String tema) {
        this.tema = tema;
        this.stsProvider = provider;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        String systemToken = stsProvider.systemToken();
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemToken);
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + systemToken);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tema=" + tema + "]";
    }

}

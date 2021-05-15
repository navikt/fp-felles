package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import javax.ws.rs.client.ClientRequestContext;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 *
 */
public class OnBehalfOfTokenRequestFilter extends OidcTokenRequestFilter {

    private final String tema;

    public OnBehalfOfTokenRequestFilter(String tema) {
        this.tema = tema;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }
}

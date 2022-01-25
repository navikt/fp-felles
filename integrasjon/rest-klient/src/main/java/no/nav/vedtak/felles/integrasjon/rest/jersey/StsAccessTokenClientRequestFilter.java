package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;

import no.nav.vedtak.exception.TekniskException;

public class StsAccessTokenClientRequestFilter extends OidcTokenRequestFilter {

    private final String tema;
    private final StsAccessTokenProvider tokenProvider;

    public StsAccessTokenClientRequestFilter(StsAccessTokenProvider provider, String tema) {
        this.tema = tema;
        this.tokenProvider = provider;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(NAV_CONSUMER_TOKEN_HEADER, OIDC_AUTH_HEADER_PREFIX + systemToken());
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + accessToken());
    }

    @Override
    protected String exchangedToken() {
        return Optional.ofNullable(samlToken())
                .map(t -> systemToken())
                .orElseThrow(() -> new TekniskException("F-937072", "Klarte ikke å fremskaffe et OIDC token"));
    }

    private String systemToken() {
        return tokenProvider.systemToken();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tema=" + tema + "]";
    }

}

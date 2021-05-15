package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXAudienceGenerator;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXJerseyClient;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 *
 */
public class TokenXTokenRequestFilter implements ClientRequestFilter {

    private final String tema;
    private final TokenXClient client;
    private final TokenXAudienceGenerator audienceGenerator;

    public TokenXTokenRequestFilter() {
        this("FOR");
    }

    public TokenXTokenRequestFilter(String tema) {
        this(tema, new TokenXJerseyClient());
    }

    public TokenXTokenRequestFilter(String tema, TokenXClient client) {
        this(tema, client, new TokenXAudienceGenerator());
    }

    public TokenXTokenRequestFilter(String tema, TokenXClient client, TokenXAudienceGenerator audienceGenerator) {
        this.tema = tema;
        this.client = client;
        this.audienceGenerator = audienceGenerator;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        ctx.getHeaders().add(TEMA, tema);
        ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + client.exchange(originalToken(), audienceGenerator.audience(ctx.getUri())));
    }

    private String originalToken() {
        return Optional.ofNullable(getSubjectHandler().getInternSsoToken()).orElseThrow();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tema=" + tema + ", client=" + client + ", audienceGenerator=" + audienceGenerator + "]";
    }
}

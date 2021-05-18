package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 *
 */
public class TokenXRequestFilter implements ClientRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(TokenXRequestFilter.class);
    private final String tema;
    private final TokenXClient client;
    private final TokenXAudienceGenerator audienceGenerator;

    public TokenXRequestFilter() {
        this("FOR");
    }

    public TokenXRequestFilter(String tema) {
        this(tema, new TokenXJerseyClient());
    }

    public TokenXRequestFilter(String tema, TokenXClient client) {
        this(tema, client, new TokenXAudienceGenerator());
    }

    public TokenXRequestFilter(String tema, TokenXClient client, TokenXAudienceGenerator audienceGenerator) {
        this.tema = tema;
        this.client = client;
        this.audienceGenerator = audienceGenerator;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        String token = originalToken();
        ctx.getHeaders().add(TEMA, tema);
        if (isTokenXToken(token)) {
            LOG.trace("Veksler tokenX token for {}", ctx.getUri());
            ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + client.exchange(token, audienceGenerator.audience(ctx.getUri())));
        } else {
            throw new IllegalStateException("Dette er intet tokenX token");
        }
    }

    private String originalToken() {
        return Optional.ofNullable(getSubjectHandler().getInternSsoToken()).orElseThrow();
    }

    private boolean isTokenXToken(String token) {
        try {
            return URI.create(SignedJWT.parse(token)
                    .getJWTClaimsSet().getIssuer()).getHost().contains("tokendings");

        } catch (Exception e) {
            LOG.warn("Kunne ikke sjekke token type", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tema=" + tema + ", client=" + client + ", audienceGenerator=" + audienceGenerator + "]";
    }
}

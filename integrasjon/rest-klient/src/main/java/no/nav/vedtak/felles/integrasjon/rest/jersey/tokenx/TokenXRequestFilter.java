package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;

import java.net.URI;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.jboss.weld.exceptions.IllegalStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 *
 */
public class TokenXRequestFilter implements ClientRequestFilter {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(TokenXRequestFilter.class);
    private final String tema;
    private final TokenXClient client;
    private final TokenXAudienceGenerator audienceGenerator;
    private final TokenXTokenProvider tokenProvider;

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
        this(tema, client, audienceGenerator, new SubjectHandlerTokenXTokenProvider());
    }

    public TokenXRequestFilter(String tema, TokenXClient client, TokenXAudienceGenerator audienceGenerator, TokenXTokenProvider tokenProvider) {
        this.tema = tema;
        this.client = client;
        this.audienceGenerator = audienceGenerator;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void filter(ClientRequestContext ctx) {
        String token = tokenProvider.getToken();
        ctx.getHeaders().add(TEMA, tema);
        if (isTokenXToken(token)) {
            LOG.trace("Veksler tokenX token for {}", ctx.getUri());
            ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + client.exchange(token, audienceGenerator.audience(ctx.getUri())));
        } else {
            if (ENV.isVTP()) {
                LOG.warn("Dette er intet tokenX token, sender originalt token videre til {} siden VTP er mangelfull her", ctx.getUri());
                ctx.getHeaders().add(AUTHORIZATION, OIDC_AUTH_HEADER_PREFIX + token);
            } else {
                throw new IllegalStateException("Dette er intet tokenX token");
            }
        }
    }

    private static boolean isTokenXToken(String token) {
        try {
            return URI.create(SignedJWT.parse(token)
                    .getJWTClaimsSet().getIssuer()).getHost().contains("tokendings");

        } catch (Exception e) {
            LOG.warn("Kunne ikke sjekke token issuer", e);
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [tema=" + tema + ", client=" + client + ", audienceGenerator=" + audienceGenerator + "]";
    }
}

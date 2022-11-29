package no.nav.vedtak.tokenx;


import static no.nav.vedtak.log.util.ConfidentialMarkerFilter.CONFIDENTIAL;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 *
 */
public final class TokenXchange {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(TokenXchange.class);

    private TokenXchange() {
        // NOSONAR
    }

    public static OpenIDToken exchange(URI targetEndpoint) {
        var token = TokenProvider.getTokenXUtenSamlFallback();
        return exchange(token, targetEndpoint);
    }

    public static OpenIDToken exchange(OpenIDToken token, URI targetEndpoint) {
        if (token != null && OpenIDProvider.TOKENX.equals(token.provider())) {
            LOG.trace(CONFIDENTIAL, "Veksler tokenX token {} for {}", token, targetEndpoint);
            var assertion = TokenXAssertionGenerator.instance().assertion();
            return TokenProvider.exchangeTokenX(token, assertion, targetEndpoint);
        } else {
            if (token != null && ENV.isLocal()) {
                LOG.warn("Dette er intet tokenX token, sender originalt token videre til {} siden VTP er mangelfull her", targetEndpoint);
                return token;
            } else {
                throw new IllegalStateException("Dette er intet tokenX token");
            }
        }
    }
}

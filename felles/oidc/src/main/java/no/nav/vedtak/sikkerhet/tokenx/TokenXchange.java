package no.nav.vedtak.sikkerhet.tokenx;


import static no.nav.vedtak.log.util.ConfidentialMarkerFilter.CONFIDENTIAL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenProvider;

/**
 * Dette filteret skal brukes når man vet man mottar et token som støtter
 * on-behalf-of, og når mottakende system krever kun dette
 */
public final class TokenXchange {
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(TokenXchange.class);

    private TokenXchange() {
    }

    public static OpenIDToken exchange(OpenIDToken token, String scopes) {
        if (token != null && OpenIDProvider.TOKENX.equals(token.provider())) {
            LOG.trace(CONFIDENTIAL, "Veksler tokenX token {} for {}", token, scopes);
            var assertion = TokenXAssertionGenerator.instance().assertion();
            return TokenProvider.exchangeTokenX(token, assertion, scopes);
        } else {
            if (token != null && ENV.isLocal()) {
                LOG.warn("Dette er intet tokenX token, sender originalt token videre til {} siden VTP er mangelfull her", scopes);
                return token;
            } else {
                throw new IllegalStateException("Dette er intet tokenX token");
            }
        }
    }
}

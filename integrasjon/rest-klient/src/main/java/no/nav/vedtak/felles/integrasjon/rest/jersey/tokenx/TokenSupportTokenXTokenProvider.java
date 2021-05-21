package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import java.util.Optional;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;

public class TokenSupportTokenXTokenProvider implements TokenXTokenProvider {

    @Override
    public String getToken() {
        return Optional.ofNullable(JaxrsTokenValidationContextHolder.getHolder())
                .map(TokenValidationContextHolder::getTokenValidationContext)
                .map(TokenValidationContext::getFirstValidToken)
                .orElse(null).map(JwtToken::getTokenAsString).orElseThrow();
    }
}

package no.nav.vedtak.sikkerhet.oidc.token.impl;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public record OidcTokenResponse(String access_token,
                                String refresh_token,
                                String token_type,
                                String scope,
                                Integer expires_in) {

    public OpenIDToken toOpenIDToken(OpenIDProvider provider) {
        return new OpenIDToken(provider, token_type, access_token, scope, expires_in);
    }

    public OpenIDToken toOpenIDToken(OpenIDProvider provider, String scope) {
        return new OpenIDToken(provider, token_type, access_token, scope, expires_in);
    }

    @Override
    public String toString() {
        return "OidcTokenResponse{" + "token_type='" + token_type + ", scope='" + (scope != null ? scope : "") + ", expires_in=" + expires_in + '}';
    }
}

package no.nav.vedtak.sikkerhet.oidc.token.impl;

public record OidcTokenResponse(String access_token,
                                String refresh_token,
                                String id_token,
                                String token_type,
                                String scope,
                                Integer expires_in) {

    @Override
    public String toString() {
        return "OidcTokenResponse{" + "token_type='" + token_type + ", scope='" + (scope != null ? scope : "") + ", expires_in=" + expires_in + '}';
    }
}

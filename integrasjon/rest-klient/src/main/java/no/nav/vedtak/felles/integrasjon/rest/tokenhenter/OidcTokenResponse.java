package no.nav.vedtak.felles.integrasjon.rest.tokenhenter;

public record OidcTokenResponse(String access_token,
                                String token_type,
                                Integer expires_in) {

    @Override
    public String toString() {
        return "OidcTokenResponse{" + "token_type=" + token_type + ", expires_in=" + expires_in + "}";
    }
}

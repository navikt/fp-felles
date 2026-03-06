package no.nav.vedtak.sikkerhet.oidc.token.texas;

public record TokenResponse(String access_token, int expires_in, String token_type) {

    @Override
    public String toString() {
        return "TokenResponse{" +
            "access_token=<redacted>'" +
            ", expires_in=" + expires_in +
            ", token_type='" + token_type +
            '}';
    }
}

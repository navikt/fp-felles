package no.nav.vedtak.sikkerhet.oidc.token.texas;

public record IntrospectTokenRequest(IdProvider identity_provider, String token) {

    @Override
    public String toString() {
        return "IntrospectTokenRequest{" +
            "identity_provider=" + identity_provider +
            ", token='<redacted>'" +
            '}';
    }
}


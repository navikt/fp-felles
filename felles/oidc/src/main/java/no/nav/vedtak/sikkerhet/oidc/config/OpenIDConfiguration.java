package no.nav.vedtak.sikkerhet.oidc.config;

import java.net.URI;

public final record OpenIDConfiguration(OpenIDProvider type,
                                        URI issuer,
                                        URI jwksUri,
                                        URI tokenEndpoint,
                                        URI authorizationEndpoint,
                                        boolean useProxyForJwks,
                                        URI proxy,
                                        String clientId,
                                        String clientSecret, // Settes nå kun for openam. Vurder økt bruk. Noen providers buker jws.
                                        boolean skipAudienceValidation) {
    @Override
    public String toString() {
        return "OpenIDConfiguration{" +
            "type=" + type +
            ", clientId='" + clientId +
            ", issuer=" + issuer +
            '}';
    }
}

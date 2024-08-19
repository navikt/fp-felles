package no.nav.vedtak.sikkerhet.oidc.config;

import java.net.URI;

public record OpenIDConfiguration(OpenIDProvider type,
                                  URI issuer,
                                  URI jwksUri,
                                  URI tokenEndpoint,
                                  boolean useProxyForJwks,
                                  URI proxy,
                                  String clientId,
                                  String clientSecret,
                                  boolean skipAudienceValidation) {
    @Override
    public String toString() {
        return "OpenIDConfiguration{" + "type=" + type + ", clientId='" + clientId + ", issuer=" + issuer + '}';
    }
}

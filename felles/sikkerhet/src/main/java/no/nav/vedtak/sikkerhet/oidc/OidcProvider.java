package no.nav.vedtak.sikkerhet.oidc;

import java.net.URI;
import java.net.URL;

public final class OidcProvider {
    private final OidcProviderType type;
    private final URL issuer;
    private final URL jwks;
    private final URI tokenEndpoint;
    private final boolean useProxyForJwks;
    private final String clientName;
    private final int allowedClockSkewInSeconds;
    private final boolean skipAudienceValidation;

    public OidcProvider(OidcProviderType type,
                        URL issuer, URL jwks, URI tokenEndpoint,
                        boolean useProxyForJwks, String clientName,
                        int allowedClockSkewInSeconds, boolean skipAudienceValidation) {
        this.type = type;
        this.issuer = issuer;
        this.jwks = jwks;
        this.useProxyForJwks = useProxyForJwks;
        this.tokenEndpoint = tokenEndpoint;
        this.clientName = clientName;
        this.allowedClockSkewInSeconds = allowedClockSkewInSeconds;
        this.skipAudienceValidation = skipAudienceValidation;
    }

    public OidcProviderType getType() {
        return type;
    }

    public URL getIssuer() {
        return issuer;
    }

    public URL getJwks() {
        return jwks;
    }

    public URI getTokenEndpoint() {
        return tokenEndpoint;
    }

    public boolean isUseProxyForJwks() {
        return useProxyForJwks;
    }

    public String getClientName() {
        return clientName;
    }

    public int getAllowedClockSkewInSeconds() {
        return allowedClockSkewInSeconds;
    }

    public boolean isSkipAudienceValidation() {
        return skipAudienceValidation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<issuer=" + issuer + ">";
    }
}

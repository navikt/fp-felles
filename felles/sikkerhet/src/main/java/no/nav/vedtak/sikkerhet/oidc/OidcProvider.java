package no.nav.vedtak.sikkerhet.oidc;

import java.net.URL;

public final class OidcProvider {
    private final URL issuer;
    private final URL jwks;
    private final boolean useProxyForJwks;
    private final String clientName;
    private final int allowedClockSkewInSeconds;
    private final boolean skipAudienceValidation;

    public OidcProvider(URL issuer, URL jwks, boolean useProxyForJwks, String clientName,
                        int allowedClockSkewInSeconds, boolean skipAudienceValidation) {
        this.issuer = issuer;
        this.jwks = jwks;
        this.useProxyForJwks = useProxyForJwks;
        this.clientName = clientName;
        this.allowedClockSkewInSeconds = allowedClockSkewInSeconds;
        this.skipAudienceValidation = skipAudienceValidation;
    }

    public URL getIssuer() {
        return issuer;
    }

    public URL getJwks() {
        return jwks;
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

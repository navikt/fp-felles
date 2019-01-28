package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.domene.IdentType;

import java.net.URL;
import java.util.Set;

public class OpenIDProviderConfig {
    private final URL issuer;
    private final URL jwks;
    private boolean useProxyForJwks;
    private final String clientName;
    private String clientPassword;
    private URL host;
    private final int allowedClockSkewInSeconds;
    private final boolean skipAudienceValidation;
    private final Set<IdentType> identTyper;

    public OpenIDProviderConfig(URL issuer, URL jwks, boolean useProxyForJwks, String clientName, String clientPassword, URL host, int allowedClockSkewInSeconds, boolean skipAudienceValidation, Set<IdentType> identTyper) {
        this.issuer = issuer;
        this.jwks = jwks;
        this.useProxyForJwks = useProxyForJwks;
        this.clientName = clientName;
        this.clientPassword = clientPassword;
        this.host = host;
        this.allowedClockSkewInSeconds = allowedClockSkewInSeconds;
        this.skipAudienceValidation = skipAudienceValidation;
        this.identTyper = identTyper;
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

    public URL getHost() {
        return host;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public int getAllowedClockSkewInSeconds() {
        return allowedClockSkewInSeconds;
    }

    public boolean isSkipAudienceValidation() {
        return skipAudienceValidation;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<issuer=" + issuer + ">"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public Set<IdentType> getIdentTyper() {
        return identTyper;
    }
}

package no.nav.vedtak.sikkerhet.context.containers;

import javax.security.auth.Destroyable;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class OidcCredential implements Destroyable {
    private boolean destroyed;
    private OpenIDToken openIDToken;

    public OidcCredential(OpenIDToken openIDToken) {
        this.openIDToken = openIDToken;
    }

    public OpenIDToken getOpenIDToken() {
        if (destroyed) {
            throw new IllegalStateException("This credential is no longer valid");
        }
        return openIDToken;
    }

    public String getToken() {
        if (destroyed) {
            throw new IllegalStateException("This credential is no longer valid");
        }
        return openIDToken.token();
    }

    @Override
    public void destroy() {
        openIDToken = null;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String toString() {
        if (destroyed) {
            return "OidcCredential[destroyed]";
        }
        return "OidcCredential[" + this.openIDToken.provider() + "," + this.openIDToken.expiresAt() + "]";
    }

}

package no.nav.vedtak.sikkerhet.domene;

import javax.security.auth.Destroyable;

public class OidcCredential implements Destroyable {
    private boolean destroyed;
    private String jwt;

    public OidcCredential(String jwt) {
        this.jwt = jwt;
    }

    public String getToken() {
        if (destroyed) {
            throw new IllegalStateException("This credential is no longer valid");
        }
        return jwt;
    }

    @Override
    public void destroy() {
        jwt = null;
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
        return "OidcCredential[" + this.jwt + "]";
    }

}

package no.nav.vedtak.sikkerhet.context.containers;

import javax.security.auth.Destroyable;

public class AuthenticationLevelCredential implements Destroyable {

    private int authenticationLevel;
    private boolean destroyed;

    public AuthenticationLevelCredential(int authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public int getAuthenticationLevel() {
        return authenticationLevel;
    }

    @Override
    public void destroy() {
        authenticationLevel = -1;
        destroyed = true;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                (destroyed ? "destroyed" : authenticationLevel) +
                "]";
    }

}

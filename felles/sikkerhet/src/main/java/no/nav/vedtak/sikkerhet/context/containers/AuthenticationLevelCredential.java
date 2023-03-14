package no.nav.vedtak.sikkerhet.context.containers;

import javax.security.auth.Destroyable;

public class AuthenticationLevelCredential implements Destroyable {

    private static final Integer AUTHENTICATION_LEVEL_INTERN_BRUKER = 4;
    private static final Integer AUTHENTICATION_LEVEL_EKSTERN_BRUKER = 4;

    public static final String AUTHENTICATION_LEVEL_ID_PORTEN = "Level" + AUTHENTICATION_LEVEL_EKSTERN_BRUKER;


    private int authenticationLevel;
    private boolean destroyed;

    public AuthenticationLevelCredential(int authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public static AuthenticationLevelCredential forInternBruker() {
        return new AuthenticationLevelCredential(AUTHENTICATION_LEVEL_INTERN_BRUKER);
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
        return getClass().getSimpleName() + "[" + (destroyed ? "destroyed" : authenticationLevel) + "]";
    }

}

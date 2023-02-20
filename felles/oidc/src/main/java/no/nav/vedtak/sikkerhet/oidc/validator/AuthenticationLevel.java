package no.nav.vedtak.sikkerhet.oidc.validator;

public class AuthenticationLevel  {

    private static final Integer AUTHENTICATION_LEVEL_INTERN_BRUKER = 4;
    private static final Integer AUTHENTICATION_LEVEL_EKSTERN_BRUKER = 4;

    public static final String AUTHENTICATION_LEVEL_ID_PORTEN = "Level" + AUTHENTICATION_LEVEL_EKSTERN_BRUKER;


    private final int authenticationLevel;

    public AuthenticationLevel(int authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public static AuthenticationLevel forInternBruker() {
        return new AuthenticationLevel(AUTHENTICATION_LEVEL_INTERN_BRUKER);
    }

    public int getAuthenticationLevel() {
        return authenticationLevel;
    }

}

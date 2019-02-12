package no.nav.vedtak.sikkerhet.abac;

public class AbacIdToken {

    private String token;
    private boolean erOidcToken;

    private AbacIdToken(String token, boolean erOidcToken) {
        this.token = token;
        this.erOidcToken = erOidcToken;
    }

    public static AbacIdToken withOidcToken(String oidcToken) {
        return new AbacIdToken(oidcToken, true);
    }

    public static AbacIdToken withSamlToken(String samlToken) {
        return new AbacIdToken(samlToken, false);
    }

    @Override
    public String toString() {
        return erOidcToken
                ? "jwtToken='" + maskerOidcToken(token) + '\''
                : "samlToken='MASKERT'";
    }

    public boolean erOidcToken() {
        return erOidcToken;
    }

    public String getToken() {
        return token;
    }

    private static String maskerOidcToken(String token) {
        return token.substring(0, token.lastIndexOf('.')) + ".MASKERT";
    }

}

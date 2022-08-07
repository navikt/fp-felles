package no.nav.vedtak.sikkerhet.abac;

public class AbacIdToken {

    public enum TokenType {
        OIDC,
        TOKENX,
        SAML;
    }

    private final String token;
    private final TokenType tokenType;

    private AbacIdToken(String token, TokenType tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    @Deprecated
    public static AbacIdToken withOidcToken(String token) {
        return withToken(token, TokenType.OIDC);
    }

    public static AbacIdToken withToken(String token, TokenType type) {
        return new AbacIdToken(token, type);
    }

    @Deprecated
    public static AbacIdToken withSamlToken(String token) {
        return withToken(token, TokenType.SAML);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    private String token() {
        return TokenType.SAML.equals(tokenType) ? "samlToken='MASKERT'" : "jwtToken='" + maskerOidcToken(token) + '\'';
    }

    @Deprecated
    public boolean erOidcToken() {
        return TokenType.OIDC.equals(tokenType);
    }

    @Deprecated
    public boolean erSamlToken() {
        return TokenType.SAML.equals(tokenType);
    }

    public String getToken() {
        return token;
    }

    private static String maskerOidcToken(String token) {
        return token.substring(0, token.lastIndexOf('.')) + ".MASKERT";
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [token=" + token() + ", tokenType=" + tokenType + "]";
    }
}

package no.nav.foreldrepenger.sikkerhet.abac.domene;

public class IdToken {

    private final String token;
    private final TokenType tokenType;

    private IdToken(String token, TokenType tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    public static IdToken withToken(String token, TokenType type) {
        return new IdToken(token, type);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [token=" + maskertToken() + ", tokenType=" + tokenType + "]";
    }

    private String maskertToken() {
        if (tokenType == TokenType.SAML) {
            return "samlToken='MASKERT'";
        }
        return "jwtToken='" + maskerOidcToken(token) + '\'';
    }

    private static String maskerOidcToken(String token) {
        return token.substring(0, token.lastIndexOf('.')) + ".MASKERT";
    }
}

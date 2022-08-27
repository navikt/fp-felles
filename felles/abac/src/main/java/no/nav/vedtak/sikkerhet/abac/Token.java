package no.nav.vedtak.sikkerhet.abac;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;

import com.nimbusds.jwt.SignedJWT;

public class Token {

    private static final String TOKENX_ISSUER = "tokendings";

    public enum TokenType {
        OIDC,
        TOKENX,
        SAML;
    }

    private final String token;
    private final TokenType tokenType;
    private final SignedJWT jwt;

    private Token(String token, TokenType tokenType, SignedJWT jwt) {
        this.token = token;
        this.tokenType = tokenType;
        this.jwt = jwt;
    }

    public static Token withOidcToken(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            var issuer = jwt.getJWTClaimsSet().getIssuer();
            var tokenType = URI.create(issuer).getHost().contains(TOKENX_ISSUER) ?
                TokenType.TOKENX : TokenType.OIDC;
            return new Token(token, tokenType, jwt);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ukjent eller malformed token", e);
        }
    }

    public static Token withSamlToken(String token) {
        return new Token(token, TokenType.SAML, null);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getToken() {
        return token;
    }

    public String getTokenBody() {
        return switch (tokenType) {
            case OIDC, TOKENX -> jwt.getPayload().toBase64URL().toString();
            case SAML -> Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [token=MASKERT, tokenType=" + tokenType + "]";
    }
}

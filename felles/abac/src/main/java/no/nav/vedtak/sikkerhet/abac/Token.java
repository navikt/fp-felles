package no.nav.vedtak.sikkerhet.abac;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class Token {

    public enum TokenType {
        OIDC,
        TOKENX,
        SAML;
    }

    private final String token;
    private final TokenType tokenType;
    private final OpenIDToken openIDToken;

    private Token(String token, TokenType tokenType, OpenIDToken openIDToken) {
        this.token = token;
        this.tokenType = tokenType;
        this.openIDToken = openIDToken;
    }

    public static Token withOidcToken(OpenIDToken token) {
        var tokenType = OpenIDProvider.TOKENX.equals(token.provider()) ? TokenType.TOKENX : TokenType.OIDC;
        return new Token(null, tokenType, token);
    }

    public static Token withSamlToken(String token) {
        return new Token(token, TokenType.SAML, null);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getTokenBody() {
        return switch (tokenType) {
            case OIDC, TOKENX -> tokenPayloadBase64(openIDToken);
            case SAML -> Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [token=MASKERT, tokenType=" + tokenType + "]";
    }

    private String tokenPayloadBase64(OpenIDToken token) {
        try {
            return SignedJWT.parse(token.token()).getPayload().toBase64URL().toString();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ukjent eller malformed token", e);
        }
    }
}

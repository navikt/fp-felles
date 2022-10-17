package no.nav.vedtak.sikkerhet.abac;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class Token {

    private static final JwtConsumer unvalidatingConsumer = new JwtConsumerBuilder()
        .setSkipAllValidators()
        .setDisableRequireSignature()
        .setSkipSignatureVerification()
        .build();

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
        return new Token(null, utledTokenType(token), token);
    }

    public static Token withSamlToken(String token) {
        return new Token(token, TokenType.SAML, null);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    private static TokenType utledTokenType(OpenIDToken token) {
        return switch (token.provider()) {
            case ISSO, STS, AZUREAD -> TokenType.OIDC;
            case TOKENX -> TokenType.TOKENX;
            case IDPORTEN -> throw new IllegalStateException("IdPorten token støttes ikke.");
        };
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

    public static String tokenPayloadBase64(OpenIDToken token) {
        try {
            var jsonObjects = unvalidatingConsumer.process(token.token()).getJoseObjects();
            var jwtBody = ((JsonWebSignature) jsonObjects.get(0)).getUnverifiedPayloadBytes();
            return org.jose4j.base64url.Base64.encode(jwtBody);
        } catch (InvalidJwtException e) {
            throw new TekniskException("F-026969", "Feil ved parsing av JWT", e);
        }
    }
}

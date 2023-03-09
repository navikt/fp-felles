package no.nav.vedtak.sikkerhet.abac;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

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
    private final String brukerId;
    private final IdentType identType;

    private Token(String token, TokenType tokenType, OpenIDToken openIDToken, String brukerId, IdentType identType) {
        this.token = token;
        this.tokenType = tokenType;
        this.openIDToken = openIDToken;
        this.brukerId = brukerId;
        this.identType = identType;
    }

    public static Token withOidcToken(OpenIDToken token, String brukerId, IdentType identType) {
        return new Token(null, utledTokenType(token), token, brukerId, identType);
    }

    public static Token withSamlToken(String token) {
        return new Token(token, TokenType.SAML, null, null, IdentType.InternBruker);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public OpenIDProvider getOpenIDProvider() {
        return Optional.ofNullable(openIDToken).map(OpenIDToken::provider).orElse(null);
    }

    public String getBrukerId() {
        return brukerId;
    }

    public IdentType getIdentType() {
        return identType;
    }

    private static TokenType utledTokenType(OpenIDToken token) {
        return switch (token.provider()) {
            case ISSO, STS, AZUREAD -> TokenType.OIDC;
            case TOKENX -> TokenType.TOKENX;
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

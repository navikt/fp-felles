package no.nav.vedtak.sikkerhet.abac;

import java.util.Optional;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class Token {

    private static final JwtConsumer unvalidatingConsumer = new JwtConsumerBuilder().setSkipAllValidators()
        .setDisableRequireSignature()
        .setSkipSignatureVerification()
        .build();

    public enum TokenType {
        OIDC,
        TOKENX;
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
            case AZUREAD -> TokenType.OIDC;
            case TOKENX -> TokenType.TOKENX;
        };
    }

    public String getTokenBody() {
        return tokenPayloadBase64(openIDToken);
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

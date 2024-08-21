package no.nav.vedtak.sikkerhet.oidc.token.impl;

import java.net.URI;
import java.util.Optional;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.TokenXProperty;

final class TokenXAssertionGenerator {

    private static final Environment ENV = Environment.current();

    private final String clientId;
    private final URI tokenEndpoint;
    private final RsaJsonWebKey privateKey;


    TokenXAssertionGenerator(OpenIDConfiguration config) {
        this(Optional.ofNullable(config).map(OpenIDConfiguration::tokenEndpoint).orElse(null),
            Optional.ofNullable(config).map(OpenIDConfiguration::clientId).orElse(null),
            Optional.ofNullable(getTokenXProperty(TokenXProperty.TOKEN_X_PRIVATE_JWK))
                .map(TokenXAssertionGenerator::rsaKey)
                .orElse(null));
    }

    // Kun til Testformål
    TokenXAssertionGenerator(URI tokenEndpoint, String clientId, RsaJsonWebKey privateKey) {
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.privateKey = privateKey;
    }

    String assertion() {
        try {
            var expirationTime = NumericDate.now();
            expirationTime.addSeconds(60);

            JwtClaims claims = new JwtClaims();
            claims.setSubject(clientId);
            claims.setIssuer(clientId);
            claims.setAudience(tokenEndpoint.toString());
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setExpirationTime(expirationTime); // Max 120s - men skal bare leve til man har fått token
            claims.setGeneratedJwtId();

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(privateKey.getPrivateKey());
            jws.setKeyIdHeaderValue(privateKey.getKeyId());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static RsaJsonWebKey rsaKey(String privateJwk) {
        try {
            var map = JsonUtil.parseJson(privateJwk);
            return new RsaJsonWebKey(map);
        } catch (JoseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String getTokenXProperty(TokenXProperty property) {
        return Optional.ofNullable(ENV.getProperty(property.name()))
            .orElseGet(() -> ENV.getProperty(property.name().toLowerCase().replace('_', '.')));
    }
}

package no.nav.vedtak.sikkerhet.oidc.token.impl;

import java.util.Optional;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

final class MaskinportenAssertionGenerator {

    private final String clientId;
    private final RsaJsonWebKey privateKey;
    private final String issuer;


    MaskinportenAssertionGenerator(String clientId, String issuer, String privateKey) {
        this(clientId, issuer, rsaKey(privateKey));
    }

    MaskinportenAssertionGenerator(String clientId, String issuer, RsaJsonWebKey privateKey) {
        this.issuer = issuer;
        this.clientId = clientId;
        this.privateKey = privateKey;
    }

    String assertion(String scope, String resource) {
        try {
            var expirationTime = NumericDate.now();
            expirationTime.addSeconds(90);
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(clientId);
            claims.setAudience(issuer);
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setExpirationTime(expirationTime); // Max 120s - men skal bare leve til man har fått token
            claims.setGeneratedJwtId();
            claims.setClaim("scope", scope);
            Optional.ofNullable(resource).ifPresent(r -> claims.setClaim("resource", r));

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
}

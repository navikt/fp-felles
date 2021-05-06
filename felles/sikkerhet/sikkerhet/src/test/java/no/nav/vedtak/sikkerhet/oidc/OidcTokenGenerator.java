package no.nav.vedtak.sikkerhet.oidc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

public class OidcTokenGenerator {
    public static final String ISSUER = "https://foo.bar.adeo.no/openam/oauth2";
    private List<String> aud = Collections.singletonList("OIDC");
    private NumericDate expiration = NumericDate.fromSeconds(NumericDate.now().getValue() + 3600);
    private String issuer = ISSUER;
    private NumericDate issuedAt = NumericDate.now();
    private String subject = "demo";
    private String kid;

    private Map<String, String> additionalClaims = new HashMap<>();

    public OidcTokenGenerator() {
        additionalClaims.put("azp", "OIDC");
    }

    OidcTokenGenerator withoutAzp() {
        additionalClaims.remove("azp");
        return this;
    }

    public OidcTokenGenerator withExpiration(NumericDate expiration) {
        this.expiration = expiration;
        return this;
    }

    OidcTokenGenerator withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    OidcTokenGenerator withIssuedAt(NumericDate issuedAt) {
        this.issuedAt = issuedAt;
        return this;
    }

    public OidcTokenGenerator withSubject(String subject) {
        this.subject = subject;
        return this;

    }

    OidcTokenGenerator withKid(String kid) {
        this.kid = kid;
        return this;

    }

    OidcTokenGenerator withClaim(String name, String value) {
        additionalClaims.put(name, value);
        return this;
    }

    OidcTokenGenerator withAud(List<String> aud) {
        this.aud = aud;
        return this;
    }

    public OidcTokenHolder createCookieTokenHolder() {
        return new OidcTokenHolder(create(), true);
    }

    public OidcTokenHolder createHeaderTokenHolder() {
        return new OidcTokenHolder(create(), false);
    }

    public String create() {
        if (kid == null) {
            kid = KeyStoreTool.getJsonWebKey().getKeyId();
        }

        JwtClaims claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setExpirationTime(expiration);
        claims.setGeneratedJwtId();
        claims.setIssuedAt(issuedAt);
        claims.setSubject(subject);
        if (aud.size() == 1) {
            claims.setAudience(aud.get(0));
        } else {
            claims.setAudience(aud);
        }
        for (Map.Entry<String, String> entry : additionalClaims.entrySet()) {
            claims.setStringClaim(entry.getKey(), entry.getValue());
        }
        RsaJsonWebKey senderJwk = KeyStoreTool.getJsonWebKey();
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKeyIdHeaderValue(kid);
        jws.setAlgorithmHeaderValue("RS256");
        jws.setKey(senderJwk.getPrivateKey());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }
}

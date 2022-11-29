package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OidcTokenGenerator {
    public static final String ISSUER = "https://foo.bar.adeo.no/azure/oauth2";
    private List<String> aud = Collections.singletonList("OIDC");
    private NumericDate expiration = NumericDate.fromSeconds(NumericDate.now().getValue() + 3600);
    private String issuer = ISSUER;
    private NumericDate issuedAt = NumericDate.now();
    private SluttBruker subject = SluttBruker.utledBruker("demo");
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

    public OidcTokenGenerator withSubject(SluttBruker subject) {
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

    public TokenString createCookieTokenHolder() {
        return new TokenString(create());
    }

    public TokenString createHeaderTokenHolder() {
        return new TokenString(create());
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
        claims.setSubject(subject.getName());
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

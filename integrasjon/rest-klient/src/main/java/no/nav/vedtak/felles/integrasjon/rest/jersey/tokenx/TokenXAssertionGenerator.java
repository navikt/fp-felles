package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

class TokenXAssertionGenerator {

    private final TokenXConfig cfg;
    private final String audience;

    TokenXAssertionGenerator(TokenXConfig cfg, URI audience) {
        this.cfg = cfg;
        this.audience = audience.toString();
    }

    public String assertion() {
        var now = Date.from(Instant.now());
        try {
            return sign(new JWTClaimsSet.Builder()
                    .subject(cfg.clientId())
                    .issuer(cfg.clientId())
                    .audience(audience)
                    .issueTime(now)
                    .notBeforeTime(now)
                    .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                    .jwtID(UUID.randomUUID().toString())
                    .build(), cfg.privateKey())
                            .serialize();
        } catch (JOSEException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static SignedJWT sign(JWTClaimsSet claimsSet, RSAKey rsaKey) throws JOSEException {
        var signedJWT = new SignedJWT(new JWSHeader.Builder(RS256)
                .keyID(rsaKey.getKeyID())
                .type(JWT).build(), claimsSet);
        signedJWT.sign(new RSASSASigner(rsaKey.toPrivateKey()));
        return signedJWT;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [cfg=" + cfg + ", audience=" + audience + "]";
    }
}

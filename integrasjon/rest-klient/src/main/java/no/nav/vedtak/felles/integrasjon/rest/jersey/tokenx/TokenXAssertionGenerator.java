package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

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
    private final TokenXConfigMetadata metadata;

    TokenXAssertionGenerator(TokenXConfig cfg, TokenXConfigMetadata metadata) {
        this.cfg = cfg;
        this.metadata = metadata;
    }

    public String assertion() {
        var now = Date.from(Instant.now());
        try {
            return sign(new JWTClaimsSet.Builder()
                    .subject(cfg.clientId())
                    .issuer(cfg.clientId())
                    .audience(metadata.tokenEndpoint().toString())
                    .issueTime(now)
                    .notBeforeTime(now)
                    .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                    .jwtID(UUID.randomUUID().toString())
                    .build(), cfg.rsaKey())
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
        return getClass().getSimpleName() + " [cfg=" + cfg + ", metadata=" + metadata + "]";
    }
}

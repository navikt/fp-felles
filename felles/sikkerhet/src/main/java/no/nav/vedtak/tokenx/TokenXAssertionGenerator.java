package no.nav.vedtak.tokenx;

import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

final class TokenXAssertionGenerator {

    private static TokenXAssertionGenerator INSTANCE;

    private final String clientId;
    private final URI tokenEndpoint;
    private final RSAKey privateKey;


    private TokenXAssertionGenerator() {
        this.tokenEndpoint = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX)
            .map(OpenIDConfiguration::tokenEndpoint).orElse(null);
        this.clientId = ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX)
            .map(OpenIDConfiguration::clientId).orElse(null);
        this.privateKey = Optional.ofNullable(Environment.current().getProperty("token.x.private.jwk"))
            .map(TokenXAssertionGenerator::rsaKey).orElse(null);
    }

    public static synchronized TokenXAssertionGenerator instance() {
        var inst = INSTANCE;
        if (inst == null) {
            inst = new TokenXAssertionGenerator();
            INSTANCE = inst;
        }
        return inst;
    }
    public String assertion() {
        var now = Date.from(Instant.now());
        try {
            var claimsSet = new JWTClaimsSet.Builder()
                .subject(clientId)
                .issuer(clientId)
                .audience(tokenEndpoint.toString())
                .issueTime(now)
                .notBeforeTime(now)
                .expirationTime(Date.from(Instant.now().plusSeconds(60)))
                .jwtID(UUID.randomUUID().toString())
                .build();
            return sign(claimsSet).serialize();
        } catch (JOSEException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private SignedJWT sign(JWTClaimsSet claimsSet) throws JOSEException {
        var jwsHeader = new JWSHeader.Builder(RS256)
            .keyID(privateKey.getKeyID())
            .type(JWT).build();
        var signedJWT = new SignedJWT(jwsHeader, claimsSet);
        signedJWT.sign(new RSASSASigner(privateKey.toPrivateKey()));
        return signedJWT;
    }

    private static RSAKey rsaKey(String privateJwk) {
        try {
            return RSAKey.parse(privateJwk);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

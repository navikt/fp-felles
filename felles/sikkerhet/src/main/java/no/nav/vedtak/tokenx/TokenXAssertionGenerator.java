package no.nav.vedtak.tokenx;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.net.URI;
import java.util.Optional;

final class TokenXAssertionGenerator {

    private static TokenXAssertionGenerator INSTANCE;

    private final String clientId;
    private final URI tokenEndpoint;
    private final RsaJsonWebKey privateKey;


    private TokenXAssertionGenerator() {
            this(ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX).map(OpenIDConfiguration::tokenEndpoint).orElse(null),
                ConfigProvider.getOpenIDConfiguration(OpenIDProvider.TOKENX).map(OpenIDConfiguration::clientId).orElse(null),
                Optional.ofNullable(Environment.current().getProperty("token.x.private.jwk")).map(TokenXAssertionGenerator::rsaKey).orElse(null));
    }

    // Kun til Testformål
    TokenXAssertionGenerator(URI tokenEndpoint, String clientId, RsaJsonWebKey privateKey) {
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.privateKey = privateKey;
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
        try {
            JwtClaims claims = new JwtClaims();
            claims.setSubject(clientId);
            claims.setIssuer(clientId);
            claims.setAudience(tokenEndpoint.toString());
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setExpirationTimeMinutesInTheFuture(1);
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
}

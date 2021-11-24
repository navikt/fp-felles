package no.nav.vedtak.sikkerhet.oidc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.jose4j.base64url.Base64;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import no.nav.vedtak.exception.TekniskException;

public class JwtUtil {

    private JwtUtil() {
    }

    private static final JwtConsumer unvalidatingConsumer = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build();

    public static String getJwtBody(String jwt) {
        try {
            var jsonObjects = unvalidatingConsumer.process(jwt).getJoseObjects();
            String jwtBody = ((JsonWebSignature) jsonObjects.get(0)).getUnverifiedPayload();
            return Base64.encode(jwtBody.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidJwtException e) {
            throw new TekniskException("F-026968", "Feil ved parsing av JWT", e);
        }
    }

    public static String getIssuer(String jwt) {
        try {
            return unvalidatingConsumer.processToClaims(jwt).getIssuer();
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new TekniskException("F-026968", "Feil ved parsing av JWT", e);
        }
    }

    public static Instant getExpirationTime(String jwt) {
        try {
            long expirationTime = unvalidatingConsumer.processToClaims(jwt).getExpirationTime().getValue();
            return Instant.ofEpochSecond(expirationTime);
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new TekniskException("F-026968", "Feil ved parsing av JWT", e);
        }
    }

    static String getClientName(String jwt) {
        try {
            JwtClaims claims = unvalidatingConsumer.processToClaims(jwt);
            String azp = claims.getStringClaimValue("azp");
            if (azp != null) {
                return azp;
            }
            List<String> audience = claims.getAudience();
            if (audience.size() == 1) {
                return audience.get(0);
            }
            throw new TekniskException("F-026678", String.format("Kan ikke utlede clientName siden 'azp' ikke er satt og 'aud' er %s", audience));
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new TekniskException("F-026968", "Feil ved parsing av JWT", e);
        }

    }

}

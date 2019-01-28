package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import org.jose4j.base64url.Base64;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class JwtUtil {

    private static JwtConsumer unvalidatingConsumer = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build();

    private JwtUtil() {}

    public static String getJwtBody(String jwt) {
        try {
            List<JsonWebStructure> jsonObjects = unvalidatingConsumer.process(jwt).getJoseObjects();
            String jwtBody = ((JsonWebSignature) jsonObjects.get(0)).getUnverifiedPayload();
            return Base64.encode(jwtBody.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidJwtException e) {
            throw JwtUtilFeil.FACTORY.ugyldigJwt(e).toException();
        }
    }

    public static String getIssuser(String jwt) {
        try {
            return unvalidatingConsumer.processToClaims(jwt).getIssuer();
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw JwtUtilFeil.FACTORY.ugyldigJwt(e).toException();
        }
    }

    public static Instant getExpirationTime(String jwt){
        try {
            long expirationTime = unvalidatingConsumer.processToClaims(jwt).getExpirationTime().getValue();
            return Instant.ofEpochSecond(expirationTime);
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw JwtUtilFeil.FACTORY.ugyldigJwt(e).toException();
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
            if(audience.size() == 1){
                return audience.get(0);
            }
            throw JwtUtilFeil.FACTORY.kanIkkeUtledeClientName(audience).toException();
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw JwtUtilFeil.FACTORY.ugyldigJwt(e).toException();
        }

    }

    interface JwtUtilFeil extends DeklarerteFeil{
        JwtUtilFeil FACTORY = FeilFactory.create(JwtUtilFeil.class);

        @TekniskFeil(feilkode = "F-026968", feilmelding = "Feil ved parsing av JWT", logLevel = LogLevel.ERROR)
        Feil ugyldigJwt(Exception e);

        @TekniskFeil(feilkode = "F-026678", feilmelding = "Kan ikke utlede clientName siden 'azp' ikke er satt og 'aud' er %s", logLevel = LogLevel.ERROR)
        Feil kanIkkeUtledeClientName(List<String> audience);
    }
}

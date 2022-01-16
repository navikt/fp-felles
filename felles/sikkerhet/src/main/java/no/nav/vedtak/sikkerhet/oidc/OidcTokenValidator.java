package no.nav.vedtak.sikkerhet.oidc;

import java.security.Key;
import java.util.List;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;
import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandler;
import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandlerImpl;
import no.nav.vedtak.sikkerhet.jwks.JwtHeader;

public class OidcTokenValidator {

    private static final Logger LOG = LoggerFactory.getLogger(OidcTokenValidator.class);

    private final String expectedIssuer;
    private final String clientName;
    private final JwksKeyHandler jwks;
    private final int allowedClockSkewInSeconds;
    private final boolean skipAudienceValidation;

    public OidcTokenValidator(OidcProvider config) {
        this(config.getIssuer().toExternalForm(), new JwksKeyHandlerImpl(config.getJwks(), config.isUseProxyForJwks(), config.getProxy()),
            config.getClientName(), config.getAllowedClockSkewInSeconds(), config.isSkipAudienceValidation());
    }

    // Skal bare brukes direkte fra tester, prod-kode skal kalle public constructors
    OidcTokenValidator(JwksKeyHandler keyHandler) {
        this(OpenAMHelper.getIssoIssuerUrl(), keyHandler, OpenAMHelper.getIssoUserName(), 30, true);

        if (this.expectedIssuer == null) {
            throw new IllegalStateException("Expected issuer must be configured.");
        }
        if (this.clientName == null) {
            throw new IllegalStateException("Expected audience must be configured.");
        }

    }

    private OidcTokenValidator(String expectedIssuer, JwksKeyHandler jwks, String clientName, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation) {
        this.expectedIssuer = expectedIssuer;
        this.jwks = jwks;
        this.clientName = clientName;
        this.allowedClockSkewInSeconds = allowedClockSkewInSeconds;
        this.skipAudienceValidation = skipAudienceValidation;
    }

    public OidcTokenValidatorResult validate(OidcTokenHolder tokenHolder) {
        return validate(tokenHolder, allowedClockSkewInSeconds);
    }

    private OidcTokenValidatorResult validate(OidcTokenHolder tokenHolder, int allowedClockSkewInSeconds) {
        LOG.trace("Validerer token for issuer {} {}", expectedIssuer, clientName);
        if (tokenHolder == null) {
            return OidcTokenValidatorResult.invalid("Missing token (token was null)");
        }
        String token = tokenHolder.token();
        JwtHeader header;
        try {
            header = getHeader(token);
        } catch (InvalidJwtException e) {
            return OidcTokenValidatorResult.invalid("Invalid OIDC " + e.getMessage());
        }
        Key validationKey = jwks.getValidationKey(header);
        if (validationKey == null) {
            return OidcTokenValidatorResult.invalid(String.format("Jwt (%s) is not in jwks", header));
        }
        JwtConsumerBuilder builder = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(allowedClockSkewInSeconds)
                .setRequireSubject()
                .setExpectedIssuer(expectedIssuer)
                .setVerificationKey(validationKey);
        if (skipAudienceValidation) {
            builder.setSkipDefaultAudienceValidation();
        } else {
            builder.setExpectedAudience(clientName);
        }
        JwtConsumer jwtConsumer = builder.build();

        try {
            JwtClaims claims = jwtConsumer.processToClaims(token);
            String error = validateClaims(claims);
            if (error != null) {
                return OidcTokenValidatorResult.invalid(error);
            }
            return OidcTokenValidatorResult.valid(claims.getSubject(), claims.getExpirationTime().getValue());
        } catch (InvalidJwtException e) {
            return OidcTokenValidatorResult.invalid(e.toString());
        } catch (MalformedClaimException e) {
            return OidcTokenValidatorResult.invalid("Malformed claim: " + e);
        }
    }

    public OidcTokenValidatorResult validateWithoutExpirationTime(OidcTokenHolder tokenHolder) {
        return validate(tokenHolder, Integer.MAX_VALUE);
    }

    // Validates some of the rules set in OpenID Connect Core 1.0 incorporating
    // errata set 1,
    // which is not already validated by using JwtConsumer
    private String validateClaims(JwtClaims claims) throws MalformedClaimException {
        String azp = claims.getStringClaimValue("azp");
        if (azp == null && claims.getAudience().size() != 1) {
            return "Either an azp-claim or a single value aud-claim is required";
        }
        return null;
    }

    private JwtHeader getHeader(String jwt) throws InvalidJwtException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setSkipAllValidators()
                .setSkipAllDefaultValidators()
                .setRelaxVerificationKeyValidation()
                .setRelaxDecryptionKeyValidation()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();

        List<JsonWebStructure> jsonObjects = jwtConsumer.process(jwt).getJoseObjects();
        JsonWebStructure wstruct = jsonObjects.get(0);
        String kid = wstruct.getKeyIdHeaderValue();
        if (kid == null) {
            kid = "";
        }
        return new JwtHeader(kid, wstruct.getAlgorithmHeaderValue());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [expectedIssuer=" + expectedIssuer + ", clientName=" + clientName + ", jwks=" + jwks
                + ", allowedClockSkewInSeconds=" + allowedClockSkewInSeconds + ", skipAudienceValidation=" + skipAudienceValidation + "]";
    }

}

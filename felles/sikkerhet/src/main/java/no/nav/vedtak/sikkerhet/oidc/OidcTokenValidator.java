package no.nav.vedtak.sikkerhet.oidc;

import no.nav.vedtak.sikkerhet.context.containers.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.context.containers.IdentType;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandler;
import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandlerImpl;
import no.nav.vedtak.sikkerhet.jwks.JwtHeader;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;

import java.security.Key;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OidcTokenValidator {

    private final OpenIDProvider provider;
    private final String expectedIssuer;
    private final String clientName;
    private final JwksKeyHandler jwks;
    private final int allowedClockSkewInSeconds;
    private final boolean skipAudienceValidation;
    private final JwtConsumer headerConsumer;

    public OidcTokenValidator(OpenIDConfiguration config) {
        this(config.type(), config.issuer().toString(), new JwksKeyHandlerImpl(config.jwksUri(), config.useProxyForJwks(), config.proxy()),
            config.clientId(), 30, config.skipAudienceValidation());
    }

    // Skal bare brukes direkte fra tester, prod-kode skal kalle public constructors
    OidcTokenValidator(OpenIDProvider provider, String issuer, JwksKeyHandler keyHandler, String clientName) {
        this(provider, issuer, keyHandler, clientName, 30, true);

        if (this.expectedIssuer == null) {
            throw new IllegalStateException("Expected issuer must be configured.");
        }
        if (this.clientName == null) {
            throw new IllegalStateException("Expected audience must be configured.");
        }

    }

    private OidcTokenValidator(OpenIDProvider provider, String expectedIssuer, JwksKeyHandler jwks, String clientName, int allowedClockSkewInSeconds,
            boolean skipAudienceValidation) {
        this.provider = provider;
        this.expectedIssuer = expectedIssuer;
        this.jwks = jwks;
        this.clientName = clientName;
        this.allowedClockSkewInSeconds = allowedClockSkewInSeconds;
        this.skipAudienceValidation = skipAudienceValidation;
        this.headerConsumer = new JwtConsumerBuilder()
            .setSkipAllValidators()
            .setSkipAllDefaultValidators()
            .setRelaxVerificationKeyValidation()
            .setRelaxDecryptionKeyValidation()
            .setDisableRequireSignature()
            .setSkipSignatureVerification()
            .build();
    }

    public OidcTokenValidatorResult validate(TokenString tokenHolder) {
        return validate(tokenHolder, allowedClockSkewInSeconds);
    }

    private OidcTokenValidatorResult validate(TokenString tokenHolder, int allowedClockSkewInSeconds) {
        if (tokenHolder == null || tokenHolder.token() == null) {
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
            String subject = claims.getSubject();
            if (OpenIDProvider.AZUREAD.equals(provider)) {
                return validateAzure(claims, subject);
            } else if (OpenIDProvider.TOKENX.equals(provider)) {
                return validateTokenX(claims, subject);
            } else {
                return OidcTokenValidatorResult.valid(SluttBruker.utledBruker(subject), claims.getExpirationTime().getValue());
            }
        } catch (InvalidJwtException e) {
            return OidcTokenValidatorResult.invalid(e.toString());
        } catch (MalformedClaimException e) {
            return OidcTokenValidatorResult.invalid("Malformed claim: " + e);
        }
    }

    public OidcTokenValidatorResult validateWithoutExpirationTime(TokenString tokenHolder) {
        return validate(tokenHolder, Integer.MAX_VALUE);
    }

    // Validates some of the rules set in OpenID Connect Core 1.0 incorporatin errata set 1,
    // which is not already validated by using JwtConsumer
    private String validateClaims(JwtClaims claims) throws MalformedClaimException {
        String azp = claims.getStringClaimValue("azp");
        if (azp == null && claims.getAudience().size() != 1) {
            return "Either an azp-claim or a single value aud-claim is required";
        }
        return null;
    }

    private OidcTokenValidatorResult validateAzure(JwtClaims claims, String subject) throws MalformedClaimException {
        if (isAzureClientCredentials(claims, subject)) {
            var brukSubject = Optional.ofNullable(claims.getStringClaimValue("azp_name")).orElse(subject);
            return OidcTokenValidatorResult.valid(new SluttBruker(brukSubject, IdentType.Systemressurs), claims.getExpirationTime().getValue());
        } else {
            var brukSubject = Optional.ofNullable(claims.getStringClaimValue("NAVident")).orElse(subject);
            return OidcTokenValidatorResult.valid(new SluttBruker(brukSubject, IdentType.InternBruker), claims.getExpirationTime().getValue());
        }
    }

    // Established practice: oid = sub -> CC-flow
    private boolean isAzureClientCredentials(JwtClaims claims, String subject) throws MalformedClaimException {
        return Objects.equals(subject, claims.getStringClaimValue("oid"));
    }

    private OidcTokenValidatorResult validateTokenX(JwtClaims claims, String subject) throws MalformedClaimException {
        var level4 = Optional.ofNullable(claims.getStringClaimValue("acr"))
            .filter(AuthenticationLevelCredential.AUTHENTICATION_LEVEL_ID_PORTEN::equals).isPresent();
        if (!level4) {
            return OidcTokenValidatorResult.invalid("TokenX token ikke på nivå 4");
        }
        var brukSubject = Optional.ofNullable(claims.getStringClaimValue("pid")).orElse(subject);
        return OidcTokenValidatorResult.valid(new SluttBruker(brukSubject, IdentType.EksternBruker), claims.getExpirationTime().getValue());
    }

    private JwtHeader getHeader(String jwt) throws InvalidJwtException {
        List<JsonWebStructure> jsonObjects = headerConsumer.process(jwt).getJoseObjects();
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

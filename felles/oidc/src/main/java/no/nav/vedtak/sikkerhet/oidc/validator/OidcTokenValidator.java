package no.nav.vedtak.sikkerhet.oidc.validator;

import static no.nav.vedtak.sikkerhet.oidc.validator.ConsumerMetric.registrer;

import java.security.Key;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.kontekst.GroupsProvider;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.AzureProperty;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.jwks.JwksKeyHandler;
import no.nav.vedtak.sikkerhet.oidc.jwks.JwksKeyHandlerImpl;
import no.nav.vedtak.sikkerhet.oidc.jwks.JwtHeader;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class OidcTokenValidator {

    private static final Logger LOG = LoggerFactory.getLogger(OidcTokenValidator.class);

    private static final Set<String> AUTHENTICATION_LEVEL_ID_PORTEN = Set.of("Level4", "idporten-loa-high"); // Level4 er gammel og utgår ila 2023

    // Misc claims and values - ikke nais-spesifikke
    private static final String ACR = "acr";
    private static final String AZP = "azp";
    private static final String OID = "oid";
    private static final String PID = "pid";
    // Optional claims for AAD/CC
    private static final String IDTYP = "idtyp";
    private static final String APP = "app";

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
    OidcTokenValidator(OpenIDConfiguration config, JwksKeyHandler keyHandler) {
        this(config.type(), config.issuer().toString(), keyHandler, config.clientId(), 30, true);
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
            String subject = JwtUtil.getSubject(claims);
            if (OpenIDProvider.AZUREAD.equals(provider)) {
                return validateAzure(claims, subject);
            } else if (OpenIDProvider.TOKENX.equals(provider)) {
                return validateTokenX(claims, subject);
            } else {
                var identType = IdentType.utledIdentType(subject);
                registrer(clientName, subject, provider, identType);
                return OidcTokenValidatorResult.valid(subject, identType, JwtUtil.getExpirationTimeRaw(claims));
            }
        } catch (InvalidJwtException e) {
            return OidcTokenValidatorResult.invalid(e.toString());
        } catch (Exception e) {
            return OidcTokenValidatorResult.invalid("Malformed claim: " + e);
        }
    }

    // Validates some of the rules set in OpenID Connect Core 1.0 incorporatin errata set 1,
    // which is not already validated by using JwtConsumer
    private String validateClaims(JwtClaims claims) {
        String azp = JwtUtil.getStringClaim(claims, AZP);
        if (azp == null && JwtUtil.getAudience(claims).size() != 1) {
            return "Either an azp-claim or a single value aud-claim is required";
        }
        return null;
    }

    private OidcTokenValidatorResult validateAzure(JwtClaims claims, String subject) {
        var oid = getAzureOid(claims);
        if (isAzureClientCredentials(claims, subject, oid)) {
            var brukSubject = Optional.ofNullable(JwtUtil.getStringClaim(claims, AzureProperty.AZP_NAME)).orElse(subject);
            registrer(clientName, brukSubject, OpenIDProvider.AZUREAD, IdentType.Systemressurs);
            // Ta med bakoverkompatibelt navn ettersom azp_name er ganske langt (tabeller / opprettet_av)
            var sisteKolon = brukSubject.lastIndexOf(':');
            if (sisteKolon >= 0) {
                var appSrvName = "srv" + brukSubject.substring(sisteKolon + 1);
                if (appSrvName.length() > 20) {
                    appSrvName = appSrvName.substring(0, 19);
                }
                return OidcTokenValidatorResult.valid(brukSubject, IdentType.Systemressurs, appSrvName, JwtUtil.getExpirationTimeRaw(claims));
            } else {
                return OidcTokenValidatorResult.valid(brukSubject, IdentType.Systemressurs, JwtUtil.getExpirationTimeRaw(claims));
            }
        } else {
            var brukSubject = Optional.ofNullable(JwtUtil.getStringClaim(claims, AzureProperty.NAV_IDENT)).orElse(subject);
            registrer(clientName, "Saksbehandler", OpenIDProvider.AZUREAD, IdentType.InternBruker);
            var grupper = Optional.ofNullable(JwtUtil.getStringListClaim(claims, AzureProperty.GRUPPER))
                    .map(arr -> GroupsProvider.instance().getGroupsFrom(arr))
                    .orElse(Set.of());
            return OidcTokenValidatorResult.valid(brukSubject, IdentType.InternBruker, oid, grupper, JwtUtil.getExpirationTimeRaw(claims));
        }
    }

    // Sjekker både gammel konvensjon (oid=sub) og nyere (idtyp="app")
    private boolean isAzureClientCredentials(JwtClaims claims, String subject, UUID oid) {
        return Objects.equals(subject, Optional.ofNullable(oid).map(UUID::toString).orElse(null)) ||
            Objects.equals(APP, JwtUtil.getStringClaim(claims, IDTYP));
    }

    private UUID getAzureOid(JwtClaims claims) {
        var oid = JwtUtil.getStringClaim(claims, OID);
        if (oid == null) {
            LOG.info("AZURE VALIDATE oid er null");
            return null;
        }
        try {
            return UUID.fromString(oid);
        } catch (Exception e) {
            LOG.info("AZURE VALIDATE kunne ikke konvertere oid til UUID {}", oid);
            return null;
        }
    }

    private OidcTokenValidatorResult validateTokenX(JwtClaims claims, String subject) {
        var acrClaim = JwtUtil.getStringClaim(claims, ACR);
        var level4 = Optional.ofNullable(acrClaim)
            .filter(AUTHENTICATION_LEVEL_ID_PORTEN::contains)
            .isPresent();
        if (!level4) {
            return OidcTokenValidatorResult.invalid("TokenX token ikke på nivå 4");
        }
        var brukSubject = Optional.ofNullable(JwtUtil.getStringClaim(claims, PID)).orElse(subject);
        registrer(clientName, "Borger", OpenIDProvider.TOKENX, IdentType.EksternBruker, acrClaim);
        return OidcTokenValidatorResult.valid(brukSubject, IdentType.EksternBruker, JwtUtil.getExpirationTimeRaw(claims));
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

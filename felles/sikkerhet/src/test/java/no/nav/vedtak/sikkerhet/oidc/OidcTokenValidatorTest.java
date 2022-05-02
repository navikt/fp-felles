package no.nav.vedtak.sikkerhet.oidc;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwt.NumericDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.jwks.JwksKeyHandlerImpl;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.config.impl.OidcProviderConfig;
import no.nav.vedtak.sikkerhet.oidc.config.impl.OpenAmProperties;
import no.nav.vedtak.sikkerhet.oidc.config.impl.WellKnownConfigurationHelper;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public class OidcTokenValidatorTest {

    private OidcTokenValidator tokenValidator;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        System.setProperty(OpenAmProperties.OPEN_ID_CONNECT_ISSO_HOST,OidcTokenGenerator.ISSUER);
        Map<String, String> testData = new HashMap<>() {
            {
                put(OpenAmProperties.ISSUER_KEY, OidcTokenGenerator.ISSUER);
            }
        };
        WellKnownConfigurationHelper.setWellKnownConfig(OidcTokenGenerator.ISSUER + OpenAmProperties.WELL_KNOWN_ENDPOINT, JsonUtil.toJson(testData));
        System.setProperty(OidcProviderConfig.OPEN_AM_CLIENT_ID, "OIDC");
        System.setProperty(OpenAmProperties.OPEN_ID_CONNECT_ISSO_ISSUER, OidcTokenGenerator.ISSUER);
        tokenValidator = new OidcTokenValidator(OpenIDProvider.ISSO, new JwksKeyHandlerFromString(KeyStoreTool.getJwks()));
    }

    @Test
    public void skal_godta_token_som_har_forventede_verdier() throws Exception {
        var token = new OidcTokenGenerator().createHeaderTokenHolder();
        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_godta_token_som_har_forventede_verdier_og_i_tillegg_har_noen_ukjente_claims() throws Exception {
        var token = new OidcTokenGenerator()
                .withClaim("email", "foo@bar.nav.no")
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_token_som_har_feil_issuer() throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 2 ..The Issuer Identifier for the OpenID provider .. MUST exactly match the
        // value of the iss (issuer) Claim.
        var token = new OidcTokenGenerator()
                .withIssuer("https://tull.nav.no")
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result,
                "rejected due to invalid claims or other invalid content. Additional details: [[12] Issuer (iss) claim value (https://tull.nav.no) doesn't match expected value of https://foo.bar.adeo.no/openam/oauth2]");
    }

    @Test
    public void skal_godta_token_uansett_audience() throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 3 ..The ID token MUST be rejected if the ID Token does not list the Client as
        // a valid audience ..

        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 3 ..The ID token MUST be rejected if the ID Token ... , or if it contains
        // additional audiences not trusted by the Client

        // Det gjøres unntak fra regelene over for å kunne bruke token på tvers i nav.
        // dette er OK siden nav er issuer og bare utsteder tokens til seg selv

        var token = new OidcTokenGenerator()
                .withAud(asList("noe"))
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_at_azp_mangler_hvis_det_er_multiple_audiences_fordi_dette_trengs_for_å_senere_kunne_gjøre_refresh_av_token()
            throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 4 If the ID Token contains multiple audiences, the Client SHOULD verify that
        // an azp Claim is present
        var token = new OidcTokenGenerator()
                .withoutAzp()
                .withAud(Arrays.asList("foo", "bar"))
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "Either an azp-claim or a single value aud-claim is required");
    }

    @Test
    public void skal_ikke_godta_at_azp_inneholder_noe_annet_enn_aktuelt_klientnavn() throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 5 If an azp (authorized party) Claim is present, the Client SHOULD verify
        // that its client_id is the Claim Value

        // Det gjøres unntak fra regelene over for å kunne bruke token på tvers i nav.
        // dette er OK siden nav er issuer og bare utsteder tokens til seg selv

        var token = new OidcTokenGenerator()
                .withClaim("azp", "noe")
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertValid(result);
    }

    @Test
    public void skal_ikke_godta_token_som_er_signert_med_feil_sertifikat() throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 6 ... The Client MUST validate the signature of all other ID Tokens according
        // to JWS using the algorithm specified in the JWT alg Header Parameter
        // The Client MUST use the keys provided by the issurer

        var token = new OidcTokenGenerator().createHeaderTokenHolder();

        OidcTokenValidator tokenValidator = new OidcTokenValidator(OpenIDProvider.ISSO, new JwksKeyHandlerFromString(
                "{\"keys\":[{\"kty\":\"RSA\",\"kid\":\"1\",\"use\":\"sig\",\"alg\":\"RS256\",\"n\":\"AM2uHZfbHbDfkCTG8GaZO2zOBDmL4sQgNzCSFqlQ-ikAwTV5ptyAHYC3JEy_LtMcRSv3E7r0yCW_7WtzT-CgBYQilb_lz1JmED3TgiThEolN2kaciY06UGycSj8wEYik-3PxuVeKr3uw6LVEohM3rrCjdlkQ_jctuvuUrCedbsb2hVw6Q17PQbWURq8v3gtXmGMD8KcR7e0dtf0ZoMOfZQoFJZ-a5dMFzXeP8Ffz_c0uBLSddd-FqOhzVDiMbvFI9XKE22TWghYanPpPsGGZYioQbJfu5VtphR6zNjiUp9O4lA_qEkbBpRA8SaUTCz3PcirFYDg0zvV8p2hgY9jyCj0\",\"e\":\"AQAB\"}]}"));
        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "JWT rejected due to invalid signature");
    }

    @Test
    public void skal_ikke_godta_token_som_har_gått_ut_på_tid() throws Exception {
        // OpenID Connect Core 1.0 incorporating errata set 1
        // 3.1.3.7 ID Token Validation
        // 9 The current time MUST be before the time represented by the exp Claim
        long now = NumericDate.now().getValue();
        var token = new OidcTokenGenerator()
                .withIssuedAt(NumericDate.fromSeconds(now - 3601))
                .withExpiration(NumericDate.fromSeconds(now - 31)).createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "is on or after the Expiration Time");
    }

    @Test
    public void skal_godta_token_som_har_gått_ut_på_tid_i_egen_metode_som_validerer_uten_tid() throws Exception {
        long now = NumericDate.now().getValue();
        var token = new OidcTokenGenerator()
                .withIssuedAt(NumericDate.fromSeconds(now - 3601))
                .withExpiration(NumericDate.fromSeconds(now - 31)).createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validateWithoutExpirationTime(token);
        assertValid(result);
    }

    @Test
    @Disabled("should fix test or use a proper framework, if setWellKnown contains errors it will be throwed before assertions")
    public void skal_ikke_godta_å_validere_token_når_det_mangler_konfigurasjon_for_issuer() throws Exception {
        WellKnownConfigurationHelper.setWellKnownConfig("openAm", "{}");
        var e = assertThrows(IllegalStateException.class, () -> new OidcTokenValidator(OpenIDProvider.ISSO, new JwksKeyHandlerFromString(KeyStoreTool.getJwks())));
        assertTrue(e.getMessage().contains("Expected issuer must be configured"));
    }

    @Test
    public void skal_ikke_godta_å_validere_token_når_det_mangler_konfigurasjon_for_audience() throws Exception {
        System.clearProperty(OidcProviderConfig.OPEN_AM_CLIENT_ID);
        var e = assertThrows(IllegalStateException.class, () -> new OidcTokenValidator(OpenIDProvider.ISSO, new JwksKeyHandlerFromString(KeyStoreTool.getJwks())));
        assertTrue(e.getMessage().contains("Expected audience must be configured"));
    }

    @Test
    public void skal_ikke_godta_token_som_har_kid_som_ikke_finnes_i_jwks() throws Exception {
        var token = new OidcTokenGenerator()
                .withKid("124135g8e")
                .createHeaderTokenHolder();

        OidcTokenValidatorResult result = tokenValidator.validate(token);
        assertInvalid(result, "124135g8e", "is not in jwks");
    }

    @Test
    public void skal_ikke_godta_null() throws Exception {
        OidcTokenValidatorResult result = tokenValidator.validate(null);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Missing token (token was null)");
    }

    @Test
    public void skal_ikke_godta_noe_som_ikke_er_et_gyldig_JWT() throws Exception {
        OidcTokenValidatorResult result1 = tokenValidator.validate(new TokenString(""));
        assertInvalid(result1,
                "Invalid OIDC JWT processing failed",
                "Invalid JOSE Compact Serialization. Expecting either 3 or 5 parts for JWS or JWE respectively but was 1.)");

        OidcTokenValidatorResult result2 = tokenValidator.validate(new TokenString("tull"));
        assertInvalid(result2,
                "Invalid OIDC JWT processing failed",
                "Invalid JOSE Compact Serialization. Expecting either 3 or 5 parts for JWS or JWE respectively but was 1.)");

        OidcTokenValidatorResult result3 = tokenValidator.validate(new TokenString("a.b.c"));
        assertInvalid(result3,
                "Invalid OIDC JWT processing failed",
                "cause: org.jose4j.lang.JoseException: Parsing error: org.jose4j.json.internal.json_simple.parser.ParseException: Unexpected token END OF FILE at position 0.): a.b.c");

        String header = "{\"kid\":\"1\", \"alg\": \"RS256\""; // mangler } på slutten
        String claims = "{\"sub\":\"demo\"}";
        String h = Base64.getEncoder().encodeToString(header.getBytes()).replaceAll("=", "");
        String p = Base64.getEncoder().encodeToString(claims.getBytes()).replaceAll("=", "");
        OidcTokenValidatorResult result4 = tokenValidator.validate(new TokenString(h + "." + p + ".123"));
        assertInvalid(result4, "Invalid OIDC JWT processing failed");
    }

    @AfterEach
    public void cleanSystemProperties() {
        System.clearProperty(OpenAmProperties.OPEN_ID_CONNECT_ISSO_HOST);
        System.clearProperty(OidcProviderConfig.OPEN_AM_CLIENT_ID);
    }

    private static class JwksKeyHandlerFromString extends JwksKeyHandlerImpl {
        private JwksKeyHandlerFromString(String jwks) throws MalformedURLException {
            super(() -> jwks, URI.create("http://www.vg.no"));
        }
    }

    private static void assertValid(OidcTokenValidatorResult result) {
        if (!result.isValid()) {
            throw new AssertionError("Forventet at token validerte, men fikk istedet feilmeldingen: " + result.getErrorMessage());
        }
    }

    private static void assertInvalid(OidcTokenValidatorResult result, String... forventet) {
        if (result.isValid()) {
            throw new AssertionError("Forventet at token feilet med feilmelding '" + asList(forventet) + "', men var OK");
        }

        for (String forventetDelAvMelding : forventet) {
            assertThat(result.getErrorMessage()).contains(forventetDelAvMelding);
        }
    }

}

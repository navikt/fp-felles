package no.nav.vedtak.sikkerhet.oidc.token.texas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class TexasSerializationTest {

    @Test
    void skal_serialisere_hentTokenRequest_korrekt() throws JsonProcessingException {
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", "https://resource.no", null);

        var json = DefaultJsonMapper.toJson(request);

        assertThat(json).contains("\"identity_provider\":\"maskinporten\"");
        assertThat(json).contains("\"target\":\"https://target.no\"");
        assertThat(json).contains("\"resource\":\"https://resource.no\"");
        assertThat(json).doesNotContain("authorization_details");
    }

    @Test
    void skal_serialisere_hentTokenRequest_med_authorization_details() throws JsonProcessingException {
        var systemuserOrg = new AuthorizationDetails.Consumer("iso6523-actorid-upis", "0192:313367002");
        var authDetails = new AuthorizationDetails(
            "urn:altinn:systemuser",
            systemuserOrg,
            List.of("33a0911a-5459-456f-bc57-3d37ef9a016c"),
            "974761076_skatt_demo_system"
        );
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "https://target.no", null, List.of(authDetails));

        var json = DefaultJsonMapper.toJson(request);

        assertThat(json).contains("\"identity_provider\":\"maskinporten\"");
        assertThat(json).contains("\"type\":\"urn:altinn:systemuser\"");
        assertThat(json).contains("\"system_id\":\"974761076_skatt_demo_system\"");
        assertThat(json).contains("\"systemuser_id\":[\"33a0911a-5459-456f-bc57-3d37ef9a016c\"]");
        assertThat(json).contains("\"ID\":\"0192:313367002\"");
        assertThat(json).contains("\"authority\":\"iso6523-actorid-upis\"");
    }

    @Test
    void skal_serialisere_exchangeTokenRequest_korrekt() throws JsonProcessingException {
        var request = new ExchangeTokenRequest(IdProvider.TOKENX, "eyJhbGciOiJSUzI1NiJ9...", "https://target.no");

        var json = DefaultJsonMapper.toJson(request);

        assertThat(json).contains("\"identity_provider\":\"tokenx\"");
        assertThat(json).contains("\"target\":\"https://target.no\"");
        assertThat(json).contains("\"user_token\":\"eyJhbGciOiJSUzI1NiJ9...\"");
    }

    @Test
    void skal_serialisere_introspectTokenRequest_korrekt() throws JsonProcessingException {
        var request = new IntrospectTokenRequest(IdProvider.ENTRA_ID, "eyJhbGciOiJSUzI1NiJ9...");

        var json = DefaultJsonMapper.toJson(request);

        assertThat(json).contains("\"identity_provider\":\"entra_id\"");
        assertThat(json).contains("\"token\":\"eyJhbGciOiJSUzI1NiJ9...\"");
    }

    @Test
    void skal_deserialisere_tokenResponse_korrekt() {
        var json = """
            {
                "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "Bearer",
                "expires_in": 3600
            }
            """;

        var response = DefaultJsonMapper.fromJson(json, TokenResponse.class);

        assertThat(response.access_token()).isEqualTo("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
        assertThat(response.token_type()).isEqualTo("Bearer");
        assertThat(response.expires_in()).isEqualTo(3600);
    }

    @Test
    void skal_deserialisere_introspectTokenResponse_aktiv() {
        var json = """
            {
                "active": true,
                "iss": "https://test.maskinporten.no/",
                "exp": 1772098539,
                "iat": 1772097939,
                "client_id": "105f1576-0e36-47f0-b5bf-01e33c1d6c97",
                "client_amr": "private_key_jwt",
                "scope": "nav:inntektsmelding/foreldrepenger nav:helseytelser/sykepenger",
                "token_type": "Bearer",
                "jti": "Jx6PBrh_k9DDiAoUrHhkf3A6jh8oXRoJ2LcZMD5ISnE",
                "consumer": {
                    "ID": "0192:312651408",
                    "authority": "iso6523-actorid-upis"
                },
                "authorization_details": [{
                    "type": "urn:altinn:systemuser",
                    "system_id": "312651408_navida",
                    "systemuser_id": ["28adb41e-4e93-469d-84db-db41943af6ac"],
                    "systemuser_org": {
                        "ID": "0192:315786940",
                        "authority": "iso6523-actorid-upis"
                    }
                }]
            }
            """;

        var response = DefaultJsonMapper.fromJson(json, IntrospectTokenResponse.class);

        assertThat(response.active()).isTrue();
        assertThat(response.error()).isNull();
        assertThat(response.iss()).isEqualTo("https://test.maskinporten.no/");
        assertThat(response.exp()).isEqualTo(1772098539L);
        assertThat(response.iat()).isEqualTo(1772097939L);
        assertThat(response.client_id()).isEqualTo("105f1576-0e36-47f0-b5bf-01e33c1d6c97");
        assertThat(response.client_amr()).isEqualTo("private_key_jwt");
        assertThat(response.scope()).isEqualTo("nav:inntektsmelding/foreldrepenger nav:helseytelser/sykepenger");
        assertThat(response.token_type()).isEqualTo("Bearer");
        assertThat(response.jti()).isEqualTo("Jx6PBrh_k9DDiAoUrHhkf3A6jh8oXRoJ2LcZMD5ISnE");

        // Consumer
        assertThat(response.consumer()).isNotNull();
        assertThat(response.consumer().id()).isEqualTo("0192:312651408");
        assertThat(response.consumer().authority()).isEqualTo("iso6523-actorid-upis");

        // Authorization details
        assertThat(response.authorization_details()).hasSize(1);
        var authDetails = response.authorization_details().getFirst();
        assertThat(authDetails.type()).isEqualTo("urn:altinn:systemuser");
        assertThat(authDetails.system_id()).isEqualTo("312651408_navida");
        assertThat(authDetails.systemuser_id()).containsExactly("28adb41e-4e93-469d-84db-db41943af6ac");
        assertThat(authDetails.systemuser_org().id()).isEqualTo("0192:315786940");
        assertThat(authDetails.systemuser_org().authority()).isEqualTo("iso6523-actorid-upis");
    }

    @Test
    void skal_deserialisere_introspectTokenResponse_inaktiv() {
        var json = """
            {
                "active": false,
                "error": "token is expired"
            }
            """;

        var response = DefaultJsonMapper.fromJson(json, IntrospectTokenResponse.class);

        assertThat(response.active()).isFalse();
        assertThat(response.error()).isEqualTo("token is expired");
        assertThat(response.iss()).isNull();
        assertThat(response.consumer()).isNull();
        assertThat(response.authorization_details()).isNull();
    }

    @Test
    void skal_deserialisere_introspectTokenResponse_med_ukjente_felter() {
        var json = """
            {
                "active": true,
                "iss": "https://test.maskinporten.no/",
                "unknown_field": "should be ignored",
                "another_unknown": 12345
            }
            """;

        var response = DefaultJsonMapper.fromJson(json, IntrospectTokenResponse.class);

        assertThat(response.active()).isTrue();
        assertThat(response.iss()).isEqualTo("https://test.maskinporten.no/");
    }

    @Test
    void skal_serialisere_idProvider_til_lowercase() throws JsonProcessingException {
        assertThat(DefaultJsonMapper.toJson(IdProvider.MASKINPORTEN)).isEqualTo("\"maskinporten\"");
        assertThat(DefaultJsonMapper.toJson(IdProvider.TOKENX)).isEqualTo("\"tokenx\"");
        assertThat(DefaultJsonMapper.toJson(IdProvider.ENTRA_ID)).isEqualTo("\"entra_id\"");
        assertThat(DefaultJsonMapper.toJson(IdProvider.IDPORTEN)).isEqualTo("\"idporten\"");
    }
}


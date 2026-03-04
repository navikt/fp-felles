package no.nav.vedtak.sikkerhet.oidc.token.texas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class HentTokenRequestTest {

    private static final ObjectMapper OBJECT_MAPPER = DefaultJsonMapper.getJsonMapper();

    @Test
    void skal_serialisere_med_alle_felter() throws JsonProcessingException {
        var systemuserOrg = new AuthorizationDetails.Consumer("iso6523-actorid-upis", "0192:11111111111");
        var authDetails = new AuthorizationDetails("urn:altinn:systemuser", systemuserOrg, List.of("33a0911a-5459-456f-bc57-3d37ef9a016c"), "11111111_skatt_demo_system");
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "target-value", "resource-value", List.of(authDetails), false);

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"maskinporten\"")
            .contains("\"target\":\"target-value\"")
            .contains("\"resource\":\"resource-value\"")
            .contains("\"authorization_details\":")
            .contains("\"type\":\"urn:altinn:systemuser\"")
            .contains("\"systemuser_org\":")
            .contains("\"ID\":\"0192:11111111111\"");
    }

    @Test
    void skal_utelate_null_verdier_ved_serialisering() throws JsonProcessingException {
        var request = new HentTokenRequest(IdProvider.TOKENX, "target-value", null, null, false);

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"tokenx\"")
            .contains("\"target\":\"target-value\"")
            .doesNotContain("resource")
            .doesNotContain("authorization_details");
    }

    @Test
    void skal_utelate_tom_liste_ved_serialisering() throws JsonProcessingException {
        var request = new HentTokenRequest(IdProvider.ENTRA_ID, "target-value", "resource-value", List.of(), false);

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"entra_id\"")
            .contains("\"target\":\"target-value\"")
            .contains("\"resource\":\"resource-value\"")
            .doesNotContain("authorization_details");
    }
}


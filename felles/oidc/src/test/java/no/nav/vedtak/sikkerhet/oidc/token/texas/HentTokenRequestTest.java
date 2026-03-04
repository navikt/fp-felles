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
        var systemuserOrg = new AuthorizationDetails.Consumer("iso6523-actorid-upis", "0192:313367002");
        var authDetails = new AuthorizationDetails("urn:altinn:systemuser", systemuserOrg, List.of("33a0911a-5459-456f-bc57-3d37ef9a016c"), "974761076_skatt_demo_system");
        var request = new HentTokenRequest(IdProvider.MASKINPORTEN, "target-value", "resource-value", List.of(authDetails));

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"maskinporten\"");
        assertThat(json).contains("\"target\":\"target-value\"");
        assertThat(json).contains("\"resource\":\"resource-value\"");
        assertThat(json).contains("\"authorization_details\":");
        assertThat(json).contains("\"type\":\"urn:altinn:systemuser\"");
        assertThat(json).contains("\"systemuser_org\":");
        assertThat(json).contains("\"ID\":\"0192:313367002\"");
    }

    @Test
    void skal_utelate_null_verdier_ved_serialisering() throws JsonProcessingException {
        var request = new HentTokenRequest(IdProvider.TOKENX, "target-value", null, null);

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"tokenx\"");
        assertThat(json).contains("\"target\":\"target-value\"");
        assertThat(json).doesNotContain("resource");
        assertThat(json).doesNotContain("authorization_details");
    }

    @Test
    void skal_utelate_tom_liste_ved_serialisering() throws JsonProcessingException {
        var request = new HentTokenRequest(IdProvider.ENTRA_ID, "target-value", "resource-value", List.of());

        var json = OBJECT_MAPPER.writeValueAsString(request);

        assertThat(json).contains("\"identity_provider\":\"entra_id\"");
        assertThat(json).contains("\"target\":\"target-value\"");
        assertThat(json).contains("\"resource\":\"resource-value\"");
        assertThat(json).doesNotContain("authorization_details");
    }
}


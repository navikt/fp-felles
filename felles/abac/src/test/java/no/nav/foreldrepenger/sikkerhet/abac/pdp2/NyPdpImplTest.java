package no.nav.foreldrepenger.sikkerhet.abac.pdp2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacAttributtNøkkel;
import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacResultat;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.IdToken;
import no.nav.foreldrepenger.sikkerhet.abac.domene.Tilgangsbeslutning;
import no.nav.foreldrepenger.sikkerhet.abac.domene.TokenType;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.Pdp;
import no.nav.foreldrepenger.sikkerhet.abac.pdp.XacmlConsumer;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.Decision;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlRequest;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml.XacmlResponse;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.NyPdpImpl;
import no.nav.foreldrepenger.sikkerhet.abac.pdp2.NyXacmlRequestMapper;
import no.nav.foreldrepenger.sikkerhet.abac.pep.PdpRequest;
import no.nav.vedtak.exception.VLException;
import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;

public class NyPdpImplTest {

    public static final String JWT_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";
    private Pdp pdpKlient;
    private NyXacmlConsumer xacmlConsumerMock;

    @BeforeEach
    public void setUp() {
        xacmlConsumerMock = mock(NyXacmlConsumer.class);
        pdpKlient = new NyPdpImpl(xacmlConsumerMock);
    }

    @Test
    public void kallPdpMedSamlTokenNårIdTokenErSamlToken() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);
        PdpRequest pdpRequest = lagPdpRequest(IdToken.withToken("<dummy SAML token>", TokenType.SAML));
        pdpRequest.setPersonnummere(Collections.singleton("12345678900"));

        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);
        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains(AbacAttributtNøkkel.ENVIRONMENT_SAML_TOKEN)).isTrue();
        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(1);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void kallPdpUtenFnrResourceHvisPersonlisteErTom() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);

        PdpRequest pdpRequest = lagPdpRequest();

        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);
        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains(AbacAttributtNøkkel.RESOURCE_PERSON_FNR)).isFalse();
        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(1);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void kallPdpMedJwtTokenBodyNårIdTokenErJwtToken() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.setPersonnummere(Collections.singleton("12345678900"));
        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);

        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains(AbacAttributtNøkkel.ENVIRONMENT_OIDC_TOKEN_BODY)).isTrue();

        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(1);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn1() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);

        PdpRequest pdpRequest = lagPdpRequest();
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");
        pdpRequest.setPersonnummere(personnr);

        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);

        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();

        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(3);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn2() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse-array.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.setPersonnummere(personnr);

        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);

        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();

        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(1);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void sporingsloggListeSkalHaSammeRekkefølgePåidenterSomXacmlRequest() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);
        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.setPersonnummere(personnr);

        var tilgangsbeslutning = pdpKlient.forespørTilgang(pdpRequest);

        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();

        assertThat(tilgangsbeslutning.fikkTilgang()).isFalse();
        assertThat(tilgangsbeslutning.getDelbeslutninger()).hasSize(3);
        assertThat(tilgangsbeslutning.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_ANNEN_ÅRSAK);
    }

    @Test
    public void skal_base64_encode_saml_token() throws Exception {
        var samlToken = "<dummy SAML token>";
        PdpRequest pdpRequest = lagPdpRequest(IdToken.withToken(samlToken, TokenType.SAML));
        pdpRequest.setPersonnummere(Collections.singleton("12345678900"));
        var request = NyXacmlRequestMapper.lagXacmlRequest(pdpRequest);
        var xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(request);
        assertThat(xacmlRequestString.contains(Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8)))).isTrue();
    }

    @Test
    public void skal_bare_ta_med_deny_advice() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse_1deny_1permit.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);
        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("07078515206");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.setPersonnummere(personnr);

        Tilgangsbeslutning resultat = pdpKlient.forespørTilgang(pdpRequest);
        assertThat(resultat.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_EGEN_ANSATT);
        assertThat(resultat.getDelbeslutninger()).isEqualTo(Arrays.asList(Decision.Deny, Decision.Permit));
    }


    @Test
    public void skalFeileVedUkjentObligation() throws Exception {
        XacmlResponse xacmlResponse = createResponse("xacmlresponse_multiple_obligation.json");
        when(xacmlConsumerMock.evaluate(any(XacmlRequest.class))).thenReturn(xacmlResponse);
        String feilKode = "";
        try {
            PdpRequest pdpRequest = lagPdpRequest(IdToken.withToken("SAML", TokenType.SAML));
            pdpRequest.setPersonnummere(Collections.singleton("12345678900"));
            pdpKlient.forespørTilgang(pdpRequest);
        } catch (VLException e) {
            feilKode = e.getKode();
        }
        assertThat(feilKode).isEqualTo("F-576027");
    }

    @Test
    public void skal_håndtere_blanding_av_fnr_og_aktør_id() throws Exception {

        XacmlResponse xacmlResponse = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequest> captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(xacmlConsumerMock.evaluate(captor.capture())).thenReturn(xacmlResponse);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        Set<String> aktørId = new HashSet<>();
        aktørId.add("11111");
        aktørId.add("22222");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.setPersonnummere(personnr);
        pdpRequest.setAktørIder(aktørId);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = DefaultJsonMapper.MAPPER.writeValueAsString(captor.getValue());

        assertThat(xacmlRequestString.contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.fnr\",\"Value\":\"12345678900\"}"))
                .isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"11111\"}")).isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"22222\"}")).isTrue();
    }

    private PdpRequest lagPdpRequest() {
        return lagPdpRequest(null);
    }

    private PdpRequest lagPdpRequest(IdToken idToken) {
        return PdpRequest.builder()
            .medDomene("foreldrepenger")
            .medActionType(ActionType.READ)
            .medResourceType("no.nav.abac.attributter.foreldrepenger.fagsak")
            .medRequest("/test/request")
            .medUserId("testUser")
            .medIdToken(idToken != null ? idToken : IdToken.withToken(JWT_TOKEN, TokenType.OIDC))
            .build();
    }

    @SuppressWarnings("resource")
    private XacmlResponse createResponse(String jsonFile) throws IOException {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(jsonFile)).getFile());
        return DefaultJsonMapper.MAPPER.readValue(file, XacmlResponse.class);
    }

}

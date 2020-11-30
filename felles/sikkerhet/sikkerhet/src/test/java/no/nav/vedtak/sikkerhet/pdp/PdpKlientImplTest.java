package no.nav.vedtak.sikkerhet.pdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

public class PdpKlientImplTest {

    public static final String JWT_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";
    private PdpKlient pdpKlient;
    private PdpConsumer pdpConsumerMock;
    private XacmlRequestBuilderTjenesteImpl xamlRequestBuilderTjeneste;

    @BeforeEach
    public void setUp() {
        pdpConsumerMock = mock(PdpConsumer.class);
        xamlRequestBuilderTjeneste = new XacmlRequestBuilderTjenesteImpl();
        pdpKlient = new PdpKlientImpl(pdpConsumerMock, xamlRequestBuilderTjeneste);
    }

    @Test
    public void kallPdpMedSamlTokenNårIdTokenErSamlToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("SAML");
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN)).isTrue();
    }

    @Test
    public void kallPdpUtenFnrResourceHvisPersonlisteErTom() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.emptySet());
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR)).isFalse();
    }

    @Test
    public void kallPdpMedJwtTokenBodyNårIdTokenErJwtToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY)).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn1() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = captor.getValue().build().toString();

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn2() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse-array.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = captor.getValue().build().toString();

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();
    }

    @Test
    public void sporingsloggListeSkalHaSammeRekkefølgePåidenterSomXacmlRequest() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        JsonObject xacmlRequest = captor.getValue().build();
        JsonArray resourceArray = xacmlRequest.getJsonObject("Request").getJsonArray("Resource");

        List<String> personer = pdpRequest.getListOfString(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR);

        for (int i = 0; i < personer.size(); i++) {
            assertThat(resourceArray.get(i).toString().contains(personer.get(i))).isTrue();
        }
    }

    @Test
    public void skal_base64_encode_saml_token() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("<dummy SAML token>");
        @SuppressWarnings("unused")
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse_multiple_obligation.json");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);

        XacmlRequestBuilder builder = xamlRequestBuilderTjeneste.lagXacmlRequestBuilder(pdpRequest);
        ((PdpKlientImpl) pdpKlient).leggPåTokenInformasjon(builder, pdpRequest);
        JsonObject jsonRequest = builder.build();
        JsonObject request = jsonRequest.getJsonObject("Request");
        JsonObject environment = request.getJsonObject("Environment");
        JsonArray attributes = environment.getJsonArray("Attribute");

        assertHasAttribute(attributes, NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN,
                Base64.getEncoder().encodeToString("<dummy SAML token>".getBytes(StandardCharsets.UTF_8)));

        attributes.getJsonObject(0).getJsonString("AttributeId");
    }

    @Test
    public void skal_bare_ta_med_deny_advice() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("<dummy SAML token>");
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse_1deny_1permit.json");

        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("07078515206");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        Tilgangsbeslutning resultat = pdpKlient.forespørTilgang(pdpRequest);
        assertThat(resultat.getBeslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_EGEN_ANSATT);
        assertThat(resultat.getDelbeslutninger()).isEqualTo(Arrays.asList(Decision.Deny, Decision.Permit));
    }

    private void assertHasAttribute(JsonArray attributes, String attributeName, String expectedValue) {
        int size = attributes.size();
        for (int i = 0; i < size; i++) {
            JsonObject obj = attributes.getJsonObject(i);
            if (obj.getJsonString("AttributeId").getString().equals(attributeName) && obj.getJsonString("Value").getString().equals(expectedValue)) {
                return;
            }
        }
        throw new AssertionError("Fant ikke " + attributeName + "=" + expectedValue + " i " + attributes);
    }

    @Test
    public void skalFeileVedUkjentObligation() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("SAML");
        XacmlResponseWrapper responseWrapper = createResponse("xacmlresponse_multiple_obligation.json");

        when(pdpConsumerMock.evaluate(any(XacmlRequestBuilder.class))).thenReturn(responseWrapper);
        String feilKode = "";
        try {
            PdpRequest pdpRequest = lagPdpRequest();
            pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("12345678900"));
            pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
            pdpKlient.forespørTilgang(pdpRequest);
        } catch (VLException e) {
            feilKode = e.getFeil().getKode();
        }
        assertThat(feilKode).isEqualTo("F-576027");
    }

    @Test
    public void skal_håndtere_blanding_av_fnr_og_aktør_id() throws FileNotFoundException {

        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        Set<String> aktørId = new HashSet<>();
        aktørId.add("11111");
        aktørId.add("22222");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, personnr);
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = captor.getValue().build().toString();

        assertThat(xacmlRequestString.contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.fnr\",\"Value\":\"12345678900\"}"))
                .isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"11111\"}")).isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"22222\"}")).isTrue();
    }

    private PdpRequest lagPdpRequest() {
        PdpRequest request = new PdpRequest();
        request.put(NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE, "foreldrepenger");
        request.put(NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID, BeskyttetRessursActionAttributt.READ.getEksternKode());
        request.put(NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, "no.nav.abac.attributter.foreldrepenger.fagsak");
        return request;
    }

    @SuppressWarnings("resource")
    private XacmlResponseWrapper createResponse(String jsonFile) throws FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource(jsonFile).getFile());
        JsonReader reader = Json.createReader(new FileReader(file));
        JsonObject jo = (JsonObject) reader.read();
        return new XacmlResponseWrapper(jo);
    }

}

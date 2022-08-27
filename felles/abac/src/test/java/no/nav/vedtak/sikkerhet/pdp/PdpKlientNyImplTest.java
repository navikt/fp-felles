package no.nav.vedtak.sikkerhet.pdp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.Token;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;
import no.nav.vedtak.sikkerhet.abac.internal.BeskyttetRessursAttributter;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.policy.ForeldrepengerAttributter;
import no.nav.vedtak.sikkerhet.pdp.xacml.Category;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequest;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;

public class PdpKlientNyImplTest {

    public static final String JWT_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";
    private static final String DOMENE = "foreldrepenger";

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
        var idToken = Token.withSamlToken("SAML");
        var responseWrapper = createResponse("xacmlresponse.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnummer("12345678900").build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        assertThat(captor.getValue().toString().contains(NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN)).isTrue();
    }

    @Test
    public void kallPdpUtenFnrResourceHvisPersonlisteErTom() throws FileNotFoundException {
        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacmlresponse.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        assertThat(captor.getValue().toString().contains(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR)).isFalse();
    }

    @Test
    public void kallPdpMedJwtTokenBodyNårIdTokenErJwtToken() throws Exception {
        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacmlresponse.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnummer("12345678900").build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        assertThat(captor.getValue().toString().contains(NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY)).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn1() throws FileNotFoundException {
        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacml3response.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnumre(personnr).build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        String xacmlRequestString = captor.getValue().toString();

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn2() throws FileNotFoundException {
        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacmlresponse-array.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnumre(personnr).build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        String xacmlRequestString = captor.getValue().toString();

        assertThat(xacmlRequestString.contains("12345678900")).isTrue();
        assertThat(xacmlRequestString.contains("00987654321")).isTrue();
        assertThat(xacmlRequestString.contains("15151515151")).isTrue();
    }

    @Test
    public void sporingsloggListeSkalHaSammeRekkefølgePåidenterSomXacmlRequest() throws FileNotFoundException {
        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacml3response.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new LinkedHashSet<>();
        personnr.add("12345678900");
        personnr.add("00987654321");
        personnr.add("15151515151");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnumre(personnr).medBehandlingStatus(BehandlingStatus.UTREDES).build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        var xacmlRequest = captor.getValue();
        var resourceArray = xacmlRequest.request().get(Category.Resource);
        var personArray = resourceArray.stream()
            .map(XacmlRequest.Attributes::attribute)
            .flatMap(Collection::stream)
            .filter(a -> NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR.equals(a.attributeId()))
            .toList();

        var personer = new ArrayList<>(ressurs.getFødselsnumre());

        for (int i = 0; i < personer.size(); i++) {
            assertThat(personArray.get(i).value().toString()).contains(personer.get(i));
        }
    }

    @Test
    public void skal_base64_encode_saml_token() throws Exception {
        var idToken = Token.withSamlToken("<dummy SAML token>");
        @SuppressWarnings("unused")
        var responseWrapper = createResponse("xacmlresponse_multiple_obligation.json");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnummer("12345678900").build();

        var jsonRequest = XacmlRequestMapper.lagXacmlRequest(felles, DOMENE, ressurs);
        var request = jsonRequest.request();
        var environment = request.get(Category.Environment);

        assertHasAttribute(environment, NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN,
                Base64.getEncoder().encodeToString("<dummy SAML token>".getBytes(StandardCharsets.UTF_8)));

        environment.get(0).attribute().get(0).attributeId();
    }

    @Test
    public void skal_bare_ta_med_deny_advice() throws Exception {
        var idToken = Token.withSamlToken("<dummy SAML token>");
        var responseWrapper = createResponse("xacmlresponse_1deny_1permit.json");

        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        personnr.add("07078515206");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnumre(personnr).build();
        var resultat = pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        assertThat(resultat.beslutningKode()).isEqualTo(AbacResultat.AVSLÅTT_EGEN_ANSATT);
    }

    private void assertHasAttribute(List<XacmlRequest.Attributes> attributes, String attributeName, String expectedValue) {
        int jsize = attributes.size();
        for (int j = 0; j < jsize; j++) {
            int size = attributes.get(j).attribute().size();
            for (int i = 0; i < size; i++) {
                var obj =  attributes.get(j).attribute().get(i);
                if (obj.attributeId().equals(attributeName) && obj.value().toString().equals(expectedValue)) {
                    return;
                }
            }
        }
        throw new AssertionError("Fant ikke " + attributeName + "=" + expectedValue + " i " + attributes);
    }

    @Test
    public void skalFeileVedUkjentObligation() throws Exception {
        var idToken = Token.withSamlToken("SAML");
        var responseWrapper = createResponse("xacmlresponse_multiple_obligation.json");

        when(pdpConsumerMock.evaluate(any(XacmlRequest.class))).thenReturn(responseWrapper);
        String feilKode = "";
        try {
            var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
            var ressurs = AppRessursData.builder().leggTilFødselsnumre(Set.of("12345678900")).build();
            pdpKlient.forespørTilgang(felles, DOMENE, ressurs);
        } catch (VLException e) {
            feilKode = e.getKode();
        }
        assertThat(feilKode).isEqualTo("F-576027");
    }

    @Test
    public void skal_håndtere_blanding_av_fnr_og_aktør_id() throws FileNotFoundException {

        var idToken = Token.withOidcToken(JWT_TOKEN);
        var responseWrapper = createResponse("xacml3response.json");
        var captor = ArgumentCaptor.forClass(XacmlRequest.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("12345678900");
        Set<String> aktørId = new HashSet<>();
        aktørId.add("11111");
        aktørId.add("22222");

        var felles = lagBeskyttetRessursAttributter(idToken, AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder().leggTilFødselsnumre(personnr).leggTilAktørIdSet(aktørId).build();
        pdpKlient.forespørTilgang(felles, DOMENE, ressurs);

        String xacmlRequestString = DefaultJsonMapper.toJson(captor.getValue());

        assertThat(xacmlRequestString.contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.fnr\",\"Value\":\"12345678900\"}"))
                .isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"11111\"}")).isTrue();
        assertThat(xacmlRequestString
                .contains("{\"AttributeId\":\"no.nav.abac.attributter.resource.felles.person.aktoerId_resource\",\"Value\":\"22222\"}")).isTrue();
    }

    private BeskyttetRessursAttributter lagBeskyttetRessursAttributter(Token token, AbacDataAttributter dataAttributter) {
        return BeskyttetRessursAttributter.builder()
            .medUserId("IDENT")
            .medToken(token)
            .medResourceType(ForeldrepengerAttributter.RESOURCE_TYPE_FP_FAGSAK)
            .medActionType(ActionType.READ)
            .medPepId("local-app")
            .medServicePath("/metode")
            .medServiceType(Token.TokenType.SAML.equals(token.getTokenType()) ? ServiceType.WEBSERVICE : ServiceType.REST)
            .medDataAttributter(dataAttributter)
            .build();
    }

    @SuppressWarnings("resource")
    private XacmlResponse createResponse(String jsonFile) {
        File file = new File(getClass().getClassLoader().getResource(jsonFile).getFile());
        try {
            return DefaultJsonMapper.getObjectMapper().readValue(file, XacmlResponse.class);
        } catch (Exception e) {
            //
        }
        return null;
    }

    @Test
    public void lese_sammenligne_request() throws IOException {
        File file = new File(getClass().getClassLoader().getResource("request.json").getFile());
        var target = DefaultJsonMapper.getObjectMapper().readValue(file, XacmlRequest.class);

        var felles = lagBeskyttetRessursAttributter(Token.withOidcToken(JWT_TOKEN), AbacDataAttributter.opprett());
        var ressurs = AppRessursData.builder()
            .leggTilAktørId("11111")
            .leggTilFødselsnummer("12345678900")
            .build();
        var request = XacmlRequestMapper.lagXacmlRequest(felles, DOMENE, ressurs);

        assertThat(request.request().get(Category.Action)).isEqualTo(target.request().get(Category.Action));
        assertThat(request.request().get(Category.Environment)).isEqualTo(target.request().get(Category.Environment));
        assertThat(request.request().get(Category.Resource)).isEqualTo(target.request().get(Category.Resource));

    }

}

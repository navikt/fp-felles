package no.nav.vedtak.felles.integrasjon.pdl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.felles.integrasjon.pdl.PdlDefaultErrorHandler.FORBUDT;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenProvider;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXRequestFilter;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.containers.SAMLAssertionCredential;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestJerseyPdlClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestJerseyPdlClient.class);
    private static final String FOR = Tema.FOR.name();
    private static final String SYSTEMTOKEN = "SYSTEMTOKEN";
    private static final String TOKENXTOKEN = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";
    private static final String GRAPHQL = "/graphql";
    private Pdl legacyClient;
    private Pdl tokenXClient;

    @Mock
    TokenXClient client;

    @Mock
    private StsAccessTokenProvider sts;
    @Mock
    private SubjectHandler subjectHandler;
    @Mock
    private SAMLAssertionCredential saml;
    private static final String CALLID = generateCallId();
    private static final String USERNAME = "ZAPHOD";
    private static WireMockServer server;
    private static URI URI;

    @BeforeAll
    static void startServer() throws Exception {
        server = new WireMockServer(0);
        server.start();
        configureFor(server.port());
        URI = new URIBuilder().setHost("localhost").setScheme("http").setPort(server.port()).setPath(GRAPHQL).build();
        putCallId(CALLID);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void beforeEach() {
        legacyClient = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
        tokenXClient = new OnBehalfOfJerseyPdlKlient(URI, new TokenXRequestFilter(FOR, client));
    }

    @Test
    @DisplayName("Test error handler")
    void testErrorHandler() {
        var handler = new PdlDefaultErrorHandler();
        var error = new GraphQLError();
        error.setExtensions(Map.of("code", FORBUDT, "details",
                Map.of("cause", "a cause", "type", "a type", "policy", "a policy")));
        var e = assertThrows(PdlException.class, () -> handler.handleError(List.of(error), URI, "KODE"));
        assertNotNull(e.getExtension());
        assertNotNull(e.getExtension().getDetails());
        assertEquals(FORBUDT, e.getExtension().getCode());
        assertEquals("a policy", e.getExtension().getDetails().getPolicy());
        assertEquals(SC_UNAUTHORIZED, e.getStatus());
    }

    @Test
    @DisplayName("Test at kun Authorization og Tema  blir satt for tokenX token")
    void testPersonAuthWithUserToken() throws Exception {
        doReturn(TOKENXTOKEN).when(sts).accessToken();
        legacyClient = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
        doReturn(new OpenIDToken(OpenIDProvider.TOKENX, new TokenString(TOKENXTOKEN))).when(subjectHandler).getOpenIDToken();
        try (var s = mockStatic(SubjectHandler.class)) {
            when(client.exchange(Mockito.any(), Mockito.any())).thenReturn(TOKENXTOKEN);
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(TOKENXTOKEN))
                    .withHeader(TEMA, equalTo(FOR))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(responseBody(responsFor("pdl/personResponse.json"))));
            var res = tokenXClient.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            res = legacyClient.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
        }
    }

    @Test
    @DisplayName("Test at Authorization blir satt til system token når vi ikke har et internt oidc token")
    void testPersonAuthWithSystemToken() throws Exception {
        doReturn(SYSTEMTOKEN).when(sts).accessToken();
        doReturn(SYSTEMTOKEN).when(sts).systemToken();
        legacyClient = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
        doReturn(saml).when(subjectHandler).getSamlToken();
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(SYSTEMTOKEN))
                    .withHeader(TEMA, equalTo(FOR))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(responseBody(responsFor("pdl/personResponse.json"))));
            var res = legacyClient.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            verify(sts, times(2)).systemToken();
        }
    }

    @Test
    @DisplayName("Test at Authorization,Nav-Consumer-Token, Nav-Consumer-Id og Tema alle blir satt tester også cache")
    void testPersonAuthWithTokenXToken() throws Exception {
        doReturn(TOKENXTOKEN).when(sts).accessToken();
        doReturn(SYSTEMTOKEN).when(sts).systemToken();
        legacyClient = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
        doReturn(new OpenIDToken(OpenIDProvider.TOKENX, new TokenString(TOKENXTOKEN))).when(subjectHandler).getOpenIDToken();
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(TOKENXTOKEN))
                    .withHeader(NAV_CONSUMER_TOKEN_HEADER, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(NAV_CONSUMER_TOKEN_HEADER, containing(SYSTEMTOKEN))
                    .withHeader(TEMA, equalTo(FOR))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(responseBody(responsFor("pdl/personResponse.json"))));
            var res = legacyClient.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            res = legacyClient.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            verify(sts, times(2)).systemToken();
            Thread.sleep(1000);
            IntStream.range(1, 10)
                    .forEach(i -> legacyClient.hentPerson(pq(), pp()));
            legacyClient.hentPerson(pq(), pp());
            verify(sts, times(12)).systemToken();
        }
    }

    @Test
    @DisplayName("Test at exception kastes når vi ikke har tokens")
    void testPersonNoTokens() throws Exception {
        when(sts.accessToken()).thenReturn(null);
        when(sts.systemToken()).thenReturn(null);
        doReturn(null).when(subjectHandler).getOpenIDToken();
        legacyClient = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL)));
            assertThrows(VLException.class, () -> legacyClient.hentPerson(pq(), pp()));
        }
    }

    private <T> GraphQLResult<T> responsFor(String fil) {
        try (var is = getClass().getClassLoader().getResourceAsStream(fil)) {
            return DefaultJsonMapper.getObjectMapper().readValue(IOUtils.toString(is, UTF_8),
                    new TypeReference<GraphQLResult<T>>() {
                    });
        } catch (Exception e) {
            throw new IllegalArgumentException("Kunne ikke konvertere " + fil + " til GraphQLResult", e);
        }
    }

    private static PersonResponseProjection pp() {
        return new PersonResponseProjection();
    }

    private static HentPersonQueryRequest pq() {
        return new HentPersonQueryRequest();
    }

    private static ResponseDefinitionBuilder responseBody(Object body) throws JsonProcessingException {
        return aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(DefaultJsonMapper.toJson(body));
    }

}

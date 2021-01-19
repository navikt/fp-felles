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
import static no.nav.vedtak.felles.integrasjon.pdl.JerseyPdlKlient.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CONSUMERID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.NAV_CONSUMER_TOKEN_HEADER;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.TEMA;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenJerseyClient;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

@ExtendWith(MockitoExtension.class)
public class TestJerseyPdlClient {

    private static final String FOR = Tema.FOR.name();
    private static final String SYSTEMTOKEN = "SYSTEMTOKEN";
    private static final String BRUKERTOKEN = "BRUKERTOKEN";
    private static final String GRAPHQL = "/graphql";
    private Pdl client;
    @Mock
    private StsAccessTokenJerseyClient sts;
    @Mock
    private SubjectHandler subjectHandler;
    @Mock
    private SAMLAssertionCredential saml;
    private static final String CALLID = generateCallId();
    private static final String USERNAME = "ZAPHOD";
    private static WireMockServer server;
    private static URI URI;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new WireMockServer(0);
        server.start();
        configureFor(server.port());
        URI = new URIBuilder().setHost("localhost").setScheme("http").setPort(server.port()).setPath(GRAPHQL).build();
        putCallId(CALLID);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        when(sts.getUsername()).thenReturn(USERNAME);
        client = new JerseyPdlKlient(URI, new StsAccessTokenClientRequestFilter(sts, FOR));
    }

    @Test
    @DisplayName("Test at Authorization, Nav-Consumer-Id, Nav-Consumer-Token, Nav-Consumer-Id og Tema alle blir satt")
    public void testPersonAuthWithUserToken() throws Exception {
        when(sts.accessToken()).thenReturn(SYSTEMTOKEN);
        doReturn(BRUKERTOKEN).when(subjectHandler).getInternSsoToken();
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(DEFAULT_NAV_CONSUMERID, equalTo(USERNAME))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(BRUKERTOKEN))
                    .withHeader(NAV_CONSUMER_TOKEN_HEADER, equalTo(SYSTEMTOKEN))
                    .withHeader(TEMA, equalTo(FOR))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(responseBody(responsFor("pdl/personResponse.json"))));
            var res = client.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            verify(sts).accessToken();
            res = client.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            verifyNoMoreInteractions(sts); // cache hit
        }
    }

    @Test
    @DisplayName("Test at Authorization blir satt til system token når vi ikke har et internt oidc token")
    public void testPersonAuthWithSystemToken() throws Exception {
        when(sts.accessToken()).thenReturn(SYSTEMTOKEN);
        doReturn(saml).when(subjectHandler).getSamlToken();
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(DEFAULT_NAV_CONSUMERID, equalTo(USERNAME))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(SYSTEMTOKEN))
                    .withHeader(NAV_CONSUMER_TOKEN_HEADER, equalTo(SYSTEMTOKEN))
                    .withHeader(TEMA, equalTo(FOR))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(responseBody(responsFor("pdl/personResponse.json"))));
            var res = client.hentPerson(pq(), pp());
            assertNotNull(res.getNavn());
            assertNotNull(res.getNavn().get(0).getFornavn());
            verify(sts).accessToken();
        }
    }

    @Test
    @DisplayName("Test at exception kastes når vi ikke har tokens")
    public void testPersonNoTokens() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(GRAPHQL)));
            assertThrows(TekniskException.class, () -> client.hentPerson(pq(), pp()));
        }
    }

    private <T> GraphQLResult<T> responsFor(String fil) {
        try (var is = getClass().getClassLoader().getResourceAsStream(fil)) {
            return mapper().readValue(IOUtils.toString(is, UTF_8),
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
                .withBody(mapper().writerWithDefaultPrettyPrinter().writeValueAsString(body));
    }

}
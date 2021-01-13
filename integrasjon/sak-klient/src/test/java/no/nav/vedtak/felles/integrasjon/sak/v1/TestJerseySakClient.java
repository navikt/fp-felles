package no.nav.vedtak.felles.integrasjon.sak.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.sak.v1.JerseySakRestKlient.FAGSAK_NR;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ExtendWith(MockitoExtension.class)
public class TestJerseySakClient {

    private static final String TOKEN = "TOKEN";
    private static final String PATH = "/api/v1/saker/";
    private static final String SAKNR = "123";
    private static final String ID = "42";
    private static SakClient CLIENT;

    @Mock
    SubjectHandler subjectHandler;
    private static final String CALLID = generateCallId();
    private static WireMockServer server;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new WireMockServer(0);
        server.start();
        configureFor(server.port());
        putCallId(CALLID);
        CLIENT = new JerseySakRestKlient(
                new URIBuilder().setHost("localhost").setScheme("http").setPort(server.port()).setPath(PATH).build());
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void beforeEach() {
        doReturn(TOKEN).when(subjectHandler).getInternSsoToken();
    }

    @Test
    public void testHentForSaksnr() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(headers(get(urlPathEqualTo(PATH)).withPort(server.port()))
                    .withQueryParam(FAGSAK_NR, new EqualToPattern(SAKNR))
                    .willReturn(responseBody(List.of(sak()))));
            var res = CLIENT.finnForSaksnummer(SAKNR);
            assertFalse(res.isEmpty());
            assertEquals(sak(), res.get());
        }
    }

    @Test
    public void testHentForSakId() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(headers(get(urlPathEqualTo(PATH + ID))).willReturn(responseBody(sak())));
            assertEquals(sak(), CLIENT.hentSakId(ID));
        }
    }

    @Test
    public void testOpprett() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(headers(post(urlPathEqualTo(PATH))).willReturn(responseBody(sak())));
            assertEquals(sak(), CLIENT.opprettSak(sak()));
        }
    }

    private static ResponseDefinitionBuilder responseBody(Object body) throws JsonProcessingException {
        return aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    }

    private static MappingBuilder headers(MappingBuilder b) {
        return b.withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withPort(server.port())
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID));
    }

    private static SakJson sak() {
        return new SakJson(1L, "2", "3", "4", "5");
    }
}
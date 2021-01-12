package no.nav.vedtak.felles.integrasjon.sak.v1;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ExtendWith(MockitoExtension.class)
public class TestJerseySakClient {

    private static final String TOKEN = "TOKEN";
    private static final String PATH = "/api/v1/saker";
    private static final String SAKNR = "123";
    private static final String ID = "42";
    private static final SakClient CLIENT = new JerseySakRestKlient("http://localhost:8080/api/v1/saker");

    @Mock
    SubjectHandler subjectHandler;
    private static String CALLID;
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void startServer() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        CALLID = generateCallId();
        putCallId(CALLID);
    }

    @AfterAll
    public static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        doReturn(TOKEN).when(subjectHandler).getInternSsoToken();

    }

    @Test
    public void testHentForSaksnr() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(get(urlPathEqualTo(PATH))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(TOKEN))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .withQueryParam("fagsakNr", new EqualToPattern(SAKNR))
                    .willReturn(aResponse()
                            .withStatus(SC_OK)
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .withBody(mapper.writeValueAsString(List.of(sak())))));
            var res = CLIENT.finnForSaksnummer(SAKNR);
            assertFalse(res.isEmpty());
            assertEquals(sak(), res.get());
        }
    }

    @Test
    public void testHentForSakId() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(get(urlPathEqualTo("/api/v1/saker/" + ID))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(TOKEN))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(aResponse()
                            .withStatus(SC_OK)
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .withBody(mapper.writeValueAsString(sak()))));
            var sak = CLIENT.hentSakId(ID);
            assertEquals(sak(), sak);
        }
    }

    @Test
    public void testOpprett() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(post(urlPathEqualTo(PATH))
                    .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                    .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                    .withHeader(AUTHORIZATION, containing(TOKEN))
                    .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID))
                    .willReturn(aResponse()
                            .withStatus(SC_OK)
                            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                            .withBody(mapper.writeValueAsString(sak()))));
            var sak = CLIENT.opprettSak(sak());
            assertEquals(sak(), sak);
        }
    }

    private static SakJson sak() {
        return new SakJson(1L, "2", "3", "4", "5");
    }
}

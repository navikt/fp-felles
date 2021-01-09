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
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import no.nav.vedtak.felles.integrasjon.rest.jersey.OidcTokenRequestFilter;

@ExtendWith(MockitoExtension.class)
public class TestJerseyClient {

    private static final String TOKEN = "TOKEN";
    private static final String PATH = "/api/v1/saker";
    private static final String SAKNR = "123";
    private static final String ID = "42";
    private JerseySakRestKlient client;
    @Spy
    private OidcTokenRequestFilter provider;
    private String callId;
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void startServer() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
    }

    @AfterAll
    public static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        callId = generateCallId();
        putCallId(callId);
        doReturn(TOKEN).when(provider).accessToken();
        client = new JerseySakRestKlient(URI.create("http://localhost:8080/api/v1/saker"), provider);
    }

    @Test
    public void testHentForSaksnr() throws Exception {
        stubFor(get(urlPathEqualTo(PATH))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(callId))
                .withQueryParam("fagsakNr", new EqualToPattern(SAKNR))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(List.of(sak())))));
        var res = client.finnForSaksnummer(SAKNR);
        assertFalse(res.isEmpty());
        verify(provider).accessToken();
        assertEquals(sak(), res.get());
    }

    @Test
    public void testHentForSakId() throws Exception {
        stubFor(get(urlPathEqualTo("/api/v1/saker/" + ID))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(callId))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(sak()))));
        var sak = client.hentSakId(ID);
        verify(provider).accessToken();
        assertEquals(sak(), sak);
    }

    @Test
    public void testOpprett() throws Exception {

        stubFor(post(urlPathEqualTo(PATH))
                .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(callId))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(sak()))));
        var sak = client.opprettSak(sak());
        verify(provider).accessToken();
        assertEquals(sak(), sak);
    }

    private static SakJson sak() {
        return new SakJson(1L, "2", "3", "4", "5");
    }
}

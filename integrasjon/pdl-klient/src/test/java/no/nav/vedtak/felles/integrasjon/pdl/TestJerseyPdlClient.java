package no.nav.vedtak.felles.integrasjon.pdl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.pdl.HentPersonBolkQueryResponse;
import no.nav.pdl.HentPersonBolkResult;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.rest.jersey.OidcTokenRequestFilter;
import no.nav.vedtak.felles.integrasjon.rest.jersey.StsAccessTokenClientRequestFilter;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Disabled
@ExtendWith(MockitoExtension.class)
public class TestJerseyPdlClient {

    private static final String TOKEN = "TOKEN";
    private static final String PATH = "/graphql";
    private Pdl client;
    @Mock
    private StsAccessTokenClientRequestFilter filter;
    @Mock
    private SubjectHandler subjectHandler;
    private static final String CALLID = generateCallId();
    private static WireMockServer server;

    @BeforeAll
    public static void startServer() throws Exception {
        server = new WireMockServer(0);
        server.start();
        configureFor(server.port());
        putCallId(CALLID);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        URI uri = new URIBuilder().setHost("localhost").setScheme("http").setPort(server.port()).setPath(PATH).build();
        client = new JerseyPdlKlient(uri, new OidcTokenRequestFilter(), filter);
        lenient().doReturn(TOKEN).when(subjectHandler).getInternSsoToken();
    }

    @Test
    public void testPerson() throws Exception {
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            stubFor(headers(post(urlPathEqualTo(PATH)))
                    .willReturn(responseBody(respons())));
            var res = client.hentPerson(pq(), pp());
            System.out.println("XXXX " + res);
        }
    }

    private PersonResponseProjection pp() {
        return new PersonResponseProjection();
    }

    private HentPersonQueryRequest pq() {
        return new HentPersonQueryRequest();
    }

    private GraphQLResult<HentPersonBolkQueryResponse> respons() {
        try (var is = getClass().getClassLoader().getResourceAsStream("pdl/personResponse.json")) {
            var res = new GraphQLResult<HentPersonBolkQueryResponse>();
            var m = new ObjectMapper();
            TypeReference<Map<String, List<HentPersonBolkResult>>> typeref = new TypeReference<Map<String, List<HentPersonBolkResult>>>() {
            };
            var r = m.readValue(IOUtils.toString(is, StandardCharsets.UTF_8), HentPersonBolkQueryResponse[].class);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
    }

    private Person person() {
        var p = new Person();
        return p;
    }

    private static ResponseDefinitionBuilder responseBody(Object body) throws JsonProcessingException {
        return aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
    }

    private static MappingBuilder headers(MappingBuilder b) {
        return b.withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID));
    }

}
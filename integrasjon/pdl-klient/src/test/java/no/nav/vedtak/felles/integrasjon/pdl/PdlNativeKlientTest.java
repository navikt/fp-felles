package no.nav.vedtak.felles.integrasjon.pdl;

import static java.util.List.of;
import static no.nav.vedtak.felles.integrasjon.pdl.PdlDefaultErrorHandler.FORBUDT;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLError;

import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkQueryResponse;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentIdenterQueryResponse;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.HentPersonQueryResponse;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.RestSender;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class PdlNativeKlientTest {

    private Pdl pdlKlient;

    @Mock
    private RestSender restClient;

    @BeforeEach
    void setUp() throws IOException {
        // Service setup
        pdlKlient = new NativePdlKlient(restClient, Tema.FOR.name());
    }

    @Test
    void skal_returnere_person() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111",
        // fagsaksystem: "AO01"}, foerste: 5)
        var resource = getClass().getClassLoader().getResource("pdl/personResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, HentPersonQueryResponse.class);
        var captor = ArgumentCaptor.forClass(RestRequest.class);

        when(restClient.send(captor.capture(), any(Class.class))).thenReturn(response);

        var query = new HentPersonQueryRequest();
        query.setIdent("12345678901");
        var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection()
                        .fornavn());

        var person = pdlKlient.hentPerson(query, projection);

        assertThat(person.getNavn().get(0).getFornavn()).isNotEmpty();
        var rq = captor.getValue();
        rq.validateRequest(r -> assertThat(r.headers().map().get("TEMA")).contains("FOR"));
        assertThat(rq.validateDelayedHeaders(Set.of("Authorization", "Nav-Consumer-Token", "Nav-Consumer-Id"))).isTrue();
    }

    @Test
    void skal_returnere_ident() throws IOException {
        var resource = getClass().getClassLoader().getResource("pdl/identerResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, HentIdenterQueryResponse.class);
        when(restClient.send(any(RestRequest.class), any())).thenReturn(response);

        var queryRequest = new HentIdenterQueryRequest();
        queryRequest.setIdent("12345678901");
        var projection = new IdentlisteResponseProjection()
                .identer(
                        new IdentInformasjonResponseProjection()
                                .ident()
                                .gruppe());

        var identer = pdlKlient.hentIdenter(queryRequest, projection);

        assertThat(identer.getIdenter()).hasSizeGreaterThan(0);
    }

    @Test
    void skal_returnere_bolk_med_identer() throws IOException {
        var resource = getClass().getClassLoader().getResource("pdl/identerBolkResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, HentIdenterBolkQueryResponse.class);
        when(restClient.send(any(RestRequest.class), any())).thenReturn(response);

        var queryRequest = new HentIdenterBolkQueryRequest();
        queryRequest.setIdenter(of("12345678901"));

        var projection = new HentIdenterBolkResultResponseProjection()
                .ident()
                .identer(new IdentInformasjonResponseProjection()
                        .ident()
                        .gruppe());
        var identer = pdlKlient.hentIdenterBolkResults(queryRequest, projection);

        assertThat(
                identer.stream()
                        .flatMap(r -> r.getIdenter().stream())
                        .map(IdentInformasjon::getIdent))
                                .containsExactlyInAnyOrder("16047439276", "9916047439276", "25017312345", "9925017312345");
    }

    @Test
    void skal_returnere_ikke_funnet() throws IOException {
        var resource = getClass().getClassLoader().getResource("pdl/errorResponse.json");
        var response = DefaultJsonMapper.fromJson(resource, HentIdenterQueryResponse.class);
        when(restClient.send(any(RestRequest.class), any())).thenReturn(response);


        var queryRequest = new HentIdenterQueryRequest();
        queryRequest.setIdent("12345678901");
        var projection = new IdentlisteResponseProjection()
                .identer(
                        new IdentInformasjonResponseProjection()
                                .ident()
                                .gruppe());

        assertThrows(PdlException.class, () -> pdlKlient.hentIdenter(queryRequest, projection));
    }

    @Test
    @DisplayName("Test error handler")
    void testErrorHandler() {
        var handler = new PdlDefaultErrorHandler();
        var error = new GraphQLError();
        error.setExtensions(Map.of("code", FORBUDT, "details",
            Map.of("cause", "a cause", "type", "a type", "policy", "a policy")));
        var e = assertThrows(PdlException.class,
            () -> handler.handleError(List.of(error), RestConfig.endpointFromAnnotation(NativePdlKlient.class), "KODE"));
        assertNotNull(e.getDetails());
        assertEquals(FORBUDT, e.getCode());
        assertEquals("a policy", e.getDetails().policy());
        assertEquals(SC_UNAUTHORIZED, e.getStatus());
    }

}

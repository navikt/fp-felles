package no.nav.vedtak.felles.integrasjon.pdl;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.pdl.HentIdenterBolkQueryRequest;
import no.nav.pdl.HentIdenterBolkResultResponseProjection;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;

@ExtendWith(MockitoExtension.class)
public class PdlKlientTest {

    private Pdl pdlKlient;

    @Mock
    private SystemConsumerStsRestClient restClient;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity httpEntity;

    @BeforeEach
    public void setUp() throws IOException {
        when(restClient.execute(any(HttpPost.class))).thenReturn(response);

        when(response.getEntity()).thenReturn(httpEntity);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));

        // Service setup
        URI endpoint = URI.create("dummyendpoint/graphql");
        pdlKlient = new PdlKlient(endpoint, Tema.OMS.name(), restClient, new PdlDefaultErrorHandler());
    }

    @Test
    public void skal_returnere_person() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111",
        // fagsaksystem: "AO01"}, foerste: 5)
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/personResponse.json"));

        var query = new HentPersonQueryRequest();
        query.setIdent("12345678901");
        var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection()
                        .fornavn());

        var person = pdlKlient.hentPerson(query, projection);

        assertThat(person.getNavn().get(0).getFornavn()).isNotEmpty();
    }

    @Test
    void skal_returnere_ident() throws IOException {
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/identerResponse.json"));

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
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/identerBolkResponse.json"));

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
                        .map(IdentInformasjon::getIdent)
        // .collect(Collectors.toList())
        )
                .containsExactlyInAnyOrder("16047439276", "9916047439276", "25017312345", "9925017312345");
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_ikke_funnet() throws IOException {
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/errorResponse.json"));

        var queryRequest = new HentIdenterQueryRequest();
        queryRequest.setIdent("12345678901");
        var projection = new IdentlisteResponseProjection()
                .identer(
                        new IdentInformasjonResponseProjection()
                                .ident()
                                .gruppe());

        assertThrows(PdlException.class, () -> pdlKlient.hentIdenter(queryRequest, projection));

    }
}

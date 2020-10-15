package no.nav.vedtak.felles.integrasjon.pdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import no.nav.pdl.*;
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

import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;

@ExtendWith(MockitoExtension.class)
public class PdlKlientTest {

    private PdlKlient pdlKlient;

    @Mock
    private SystemConsumerStsRestClient restClient;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity httpEntity;


    @SuppressWarnings("resource")
    @BeforeEach
    public void setUp() throws IOException {
        // POST mock
        when(restClient.execute(any(HttpPost.class))).thenReturn(response);

        // response mock
        when(response.getEntity()).thenReturn(httpEntity);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));

        // Service setup
        URI endpoint = URI.create("dummyendpoint/graphql");
        pdlKlient = new PdlKlient(endpoint, restClient);
    }


    @SuppressWarnings("resource")
    @Test
    public void skal_returnere_person() throws IOException {
        //query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111", fagsaksystem: "AO01"}, foerste: 5)
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/personResponse.json"));

        var query = new HentPersonQueryRequest();
        query.setIdent("12345678901");
        var projection = new PersonResponseProjection()
            .navn(new NavnResponseProjection()
                .fornavn());

        var person = pdlKlient.hentPerson(query, projection, Tema.OMS);

        assertThat(person.getNavn().get(0).getFornavn()).isNotEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_ident() throws IOException {
        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/identerResponse.json"));

        var queryRequest = new HentIdenterQueryRequest();
        queryRequest.setIdent("12345678901");
        var projection = new IdentlisteResponseProjection()
            .identer(
                new IdentInformasjonResponseProjection()
                    .ident()
                    .gruppe()
            );

        var identer = pdlKlient.hentIdenter(queryRequest, projection, Tema.OMS);

        assertThat(identer.getIdenter()).hasSizeGreaterThan(0);
    }
}

package no.nav.vedtak.felles.integrasjon.pdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

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
public class PdlKlientMedCacheTest {

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


    @Test
    void skal_hente_aktørId_personIdent() throws IOException {

        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/identerResponse.json"));

        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlient);

        Optional<String> s = testSubject.hentAktørIdForPersonIdent("16047439276", Tema.OMS);

        assertThat(s).isNotEmpty();
    }

    @Test
    void skal_hente_personIdent_for_aktørId() throws IOException {

        when(httpEntity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("pdl/identerResponse.json"));

        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlient);

        Optional<String> s = testSubject.hentPersonIdentForAktørId("9916047439276", Tema.OMS);

        assertThat(s).isNotEmpty();
    }


//    @Test
//    void skal_hent_aktørId_for_set_med_personIdent() {
//
//        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlient);
//        Set<String> aktørIds = testSubject.hentAktørIdForPersonIdentSet(Set.of("16047439276"));
//
//        assertThat(aktørIds).hasSize(1);
//        assertThat(aktørIds).contains("9916047439276");
//    }
}

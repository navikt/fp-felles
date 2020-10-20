package no.nav.vedtak.felles.integrasjon.pdl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;
import no.nav.vedtak.util.LRUCache;

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

        Optional<String> s = testSubject.hentAktørIdForPersonIdent("16047439276");

        Assertions.assertThat(s).isNotEmpty();
    }
}

package no.nav.vedtak.felles.integrasjon.pdl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.felles.integrasjon.rest.SystemConsumerStsRestClient;
import no.nav.vedtak.util.LRUCache;

@ExtendWith(MockitoExtension.class)
public class PdlKlientMedCacheInitsialisertTest {

    @Test
    void skal_hente_cachet_personIdent() {
        PdlKlient pdlKlientMock = mock(PdlKlient.class);
        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlientMock, enTomCache(), enCacheMed("16047439276", "9916047439276"));

        Optional<String> s = testSubject.hentAktørIdForPersonIdent("16047439276");

        Assertions.assertThat(s).isNotEmpty();

        verify(pdlKlientMock, never()).hentIdenter(any(HentIdenterQueryRequest.class), any(IdentlisteResponseProjection.class), any(Tema.class));
    }

    private LRUCache enCacheMed(String key, String value) {
        // Oprrettt en tom cache
        // put inn innslag
        LRUCache c = new LRUCache(20, 3600L);
        c.put(key, Optional.of(value));
        return c;
    }

    private LRUCache enTomCache() {
        return new LRUCache(20, 3600L);
    }
}

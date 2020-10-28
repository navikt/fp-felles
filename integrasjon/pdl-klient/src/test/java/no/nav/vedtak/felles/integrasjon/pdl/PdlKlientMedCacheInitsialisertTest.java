package no.nav.vedtak.felles.integrasjon.pdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.util.LRUCache;

@ExtendWith(MockitoExtension.class)
public class PdlKlientMedCacheInitsialisertTest {

    @Test
    void skal_hente_cachet_personIdent() {
        PdlKlient pdlKlientMock = mock(PdlKlient.class);
        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlientMock, enTomCache(), enCacheMed("16047439276", "9916047439276"));

        assertThat(
            testSubject.hentAktørIdForPersonIdent("16047439276", Tema.OMS)
        )
            .contains("9916047439276");

        verify(pdlKlientMock, never()).hentIdenter(any(HentIdenterQueryRequest.class), any(IdentlisteResponseProjection.class), any(Tema.class));
    }

    @SuppressWarnings("SameParameterValue")
    private LRUCache<String, String> enCacheMed(String key, String value) {
        LRUCache<String, String> c = new LRUCache<>(20, 3600L);
        c.put(key, value);
        return c;
    }

    private LRUCache<String, String> enTomCache() {
        return new LRUCache<>(20, 3600L);
    }
}

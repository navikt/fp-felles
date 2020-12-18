package no.nav.vedtak.felles.integrasjon.pdl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentlisteResponseProjection;

@ExtendWith(MockitoExtension.class)
public class PdlKlientMedCacheInitsialisertTest {

    @Test
    void skal_hente_cachet_personIdent() {
        PdlKlient pdlKlientMock = mock(PdlKlient.class);
        PdlKlientMedCache testSubject = new PdlKlientMedCache(pdlKlientMock, enTomCache(), enCacheMed("16047439276", "9916047439276"));

        assertThat(
                testSubject.hentAktørIdForPersonIdent("16047439276", Tema.OMS))
                        .contains("9916047439276");

        verify(pdlKlientMock, never()).hentIdenter(any(HentIdenterQueryRequest.class), any(IdentlisteResponseProjection.class), any(Tema.class));
    }

    private Cache<String, String> enCacheMed(String key, String value) {
        Cache<String, String> c = Caffeine.newBuilder()
                .expireAfterWrite(3600L, TimeUnit.MILLISECONDS)
                .maximumSize(20)
                .build();
        c.put(key, value);
        return c;

    }

    private Cache<String, String> enTomCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(3600L, TimeUnit.MILLISECONDS)
                .maximumSize(20).build();
    }
}

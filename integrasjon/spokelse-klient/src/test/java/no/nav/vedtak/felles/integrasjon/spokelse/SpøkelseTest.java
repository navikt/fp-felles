package no.nav.vedtak.felles.integrasjon.spokelse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SpøkelseTest {

    private Spøkelse spøkelse;

    @Mock
    private RestClient restKlient;

    @BeforeEach
    void setUp() throws IOException {
        // Service setup
        spøkelse = new SpøkelseNativeKlient(restKlient);
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_dokumentoversikt_fagsak() throws IOException {
        // query-eksempel: dokumentoversiktFagsak(fagsak: {fagsakId: "2019186111",
        // fagsaksystem: "AO01"}, foerste: 5)
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        SykepengeVedtak[] svar = {new SykepengeVedtak("abc", List.of(), LocalDateTime.now())};
        when(restKlient.send(captor.capture(), any(Class.class))).thenReturn(svar);

        var respons = spøkelse.hentGrunnlag("11111111111");
        var rq = captor.getValue();
        rq.validateDelayedHeaders(Set.of("Authorization"));
    }


}

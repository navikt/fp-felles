package no.nav.vedtak.felles.integrasjon.spokelse;

import static org.assertj.core.api.Assertions.assertThat;
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
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class SpøkelseTest {

    private Spøkelse spøkelse;

    @Mock
    private RestClient restKlient;

    @BeforeEach
    void setUp() throws IOException {
        // Service setup
        spøkelse = new TestSpøkelse(restKlient);
    }

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_dokumentoversikt_fagsak()  {
        var captor = ArgumentCaptor.forClass(RestRequest.class);
        SykepengeVedtak[] svar = {new SykepengeVedtak("abc", List.of(), LocalDateTime.now())};
        when(restKlient.send(captor.capture(), any())).thenReturn(svar);

        var respons = spøkelse.hentGrunnlag("11111111111");
        var rq = captor.getValue();
        rq.validateDelayedHeaders(Set.of("Authorization"));
        assertThat(respons.get(0).vedtaksreferanse()).isEqualTo("abc");
    }

    @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "SPOKELSE_GRUNNLAG_URL", endpointDefault = "http://spokelse.tbd/grunnlag",
        scopesProperty = "SPOKELSE_GRUNNLAG_SCOPES", scopesDefault = "api://prod-fss.tbd.spokelse/.default")
    private static class TestSpøkelse extends AbstractSpøkelseKlient {
        public TestSpøkelse(RestClient restKlient) {
            super(restKlient);
        }
    }

}

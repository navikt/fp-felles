package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
class AbstractOppgaveKlientTest {
    private Oppgaver oppgaver;

    @Mock
    private RestClient restKlient;

    @BeforeEach
    void setUp() {
        // Service setup
        oppgaver = new TestOppgave(restKlient);
    }

    private static final String json = """
        {
          "id": 357736794,
          "tildeltEnhetsnr":"1234",
          "opprettetAvEnhetsnr":"1234",
          "saksreferanse":"152200771",
          "aktoerId":"1000000000000",
          "beskrivelse":"Må behandle sak i VL!",
          "temagruppe":"FMLI",
          "tema":"FOR",
          "behandlingstema":"ab0326",
          "oppgavetype":"BEH_SAK",
          "versjon":1,
          "opprettetAv":"srvengangsstonad",
          "prioritet":"NORM",
          "status": "OPPRETTET",
          "metadata":{},
          "fristFerdigstillelse":"2023-01-23",
          "aktivDato":"2023-01-22",
          "opprettetTidspunkt":"2023-01-22T18:52:45.206+01:00"
        }
        """;

    @SuppressWarnings("resource")
    @Test
    void skal_returnere_dokumentoversikt_fagsak() {
        var captor = ArgumentCaptor.forClass(RestRequest.class);

        when(restKlient.send(any(RestRequest.class), eq(Oppgave.class))).thenReturn(DefaultJsonMapper.fromJson(json, Oppgave.class));

        when(restKlient.sendExpectConflict(captor.capture(), Oppgave.class))

        oppgaver.reserverOppgave("1", "testSbh");

        var rq = captor.getValue();
        rq.validateDelayedHeaders(Set.of("Authorization"));
        //assertThat(respons.get(0).vedtaksreferanse()).isEqualTo("abc");
    }

    @RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "OPPGAVE_GRUNNLAG_URL", endpointDefault = "http://oppgave.tbd/", scopesProperty = "OPPGAVE_GRUNNLAG_SCOPES", scopesDefault = "api://prod-fss.tbd.oppgave/.default")
    private static class TestOppgave extends AbstractOppgaveKlient {
        public TestOppgave(RestClient restKlient) {
            super(restKlient);
        }
    }
}

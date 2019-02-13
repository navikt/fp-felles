package no.nav.vedtak.felles.integrasjon.oppgave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.HentOppgaveOppgaveIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppgave.v3.binding.OppgaveV3;
import no.nav.tjeneste.virksomhet.oppgave.v3.informasjon.oppgave.Oppgave;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveResponse;

public class OppgaveConsumerImplTest {
    private OppgaveV3 port;
    private OppgaveConsumerImpl consumer;

    @Before
    public void setup() {
        port = mock(OppgaveV3.class);
        consumer = new OppgaveConsumerImpl(port);
    }

    @Test
    public void skalReturnereOppgaveListeResponse() {
        // Arrange
        FinnOppgaveListeRequestMal requestMal = new FinnOppgaveListeRequestMal(lagFinnOppgaveListeSokMal(), null, null, null);
        FinnOppgaveListeResponse oppgaveListeResponse = lagConsumerFinnOppgaveListeResponse();
        when(port.finnOppgaveListe(any(FinnOppgaveListeRequest.class))).thenReturn(oppgaveListeResponse);

        // Act
        FinnOppgaveListeResponse response = consumer.finnOppgaveListe(requestMal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(oppgaveListeResponse);

    }

    @Test
    public void skalReturnereOppgaveResponse() throws HentOppgaveOppgaveIkkeFunnet {
        HentOppgaveRequest request = new HentOppgaveRequest();
        request.setOppgaveId("123123");
        HentOppgaveResponse hentOppgaveResponse = lagConsumerHentOppgaveResponse();
        when(port.hentOppgave(any(HentOppgaveRequest.class))).thenReturn(hentOppgaveResponse);

        HentOppgaveResponse response = consumer.hentOppgave(request);

        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(hentOppgaveResponse);
    }

    private FinnOppgaveListeSokMal lagFinnOppgaveListeSokMal() {
        FinnOppgaveListeSokMal.Builder builder = FinnOppgaveListeSokMal.builder();
        return builder.medAnsvarligEnhetId("0123").build();
    }

    private FinnOppgaveListeResponse lagConsumerFinnOppgaveListeResponse() {
        FinnOppgaveListeResponse response = new FinnOppgaveListeResponse();
        response.setTotaltAntallTreff(5);
        response.getOppgaveListe().add(new Oppgave());
        return response;
    }

    private HentOppgaveResponse lagConsumerHentOppgaveResponse() {
        HentOppgaveResponse response = new HentOppgaveResponse();
        response.setOppgave(new Oppgave());
        return response;
    }
}
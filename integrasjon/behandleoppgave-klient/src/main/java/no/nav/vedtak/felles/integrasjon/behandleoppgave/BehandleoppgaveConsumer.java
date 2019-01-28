package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSFerdigstillOppgaveResponse;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;

public interface BehandleoppgaveConsumer {
    WSOpprettOppgaveResponse opprettOppgave(OpprettOppgaveRequest request);

    WSFerdigstillOppgaveResponse ferdigstillOppgave(FerdigstillOppgaveRequestMal request);
}

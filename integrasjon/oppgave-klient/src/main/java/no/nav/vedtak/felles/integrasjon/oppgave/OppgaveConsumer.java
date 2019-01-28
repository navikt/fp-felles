package no.nav.vedtak.felles.integrasjon.oppgave;

import no.nav.tjeneste.virksomhet.oppgave.v3.binding.HentOppgaveOppgaveIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.FinnOppgaveListeResponse;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveRequest;
import no.nav.tjeneste.virksomhet.oppgave.v3.meldinger.HentOppgaveResponse;

public interface OppgaveConsumer {
    FinnOppgaveListeResponse finnOppgaveListe(FinnOppgaveListeRequestMal request);

    HentOppgaveResponse hentOppgave(HentOppgaveRequest request) throws HentOppgaveOppgaveIkkeFunnet;
}

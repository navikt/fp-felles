package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;

import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave.Builder;

public interface Oppgaver {

    Oppgave hentOppgave(String oppgaveId);

    void feilregistrerOppgave(String oppgaveId);

    void ferdigstillOppgave(String oppgaveId);

    List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception;

    List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception;

    @Deprecated(since = "3.0.x", forRemoval = true)
    default Oppgave opprettetOppgave(Builder requestBuilder) {
        return opprettetOppgave(requestBuilder.build());
    }

    Oppgave opprettetOppgave(OpprettOppgave oppgave);

}

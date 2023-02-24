package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;

public interface Oppgaver {

    Oppgave opprettetOppgave(OpprettOppgave oppgave);

    Oppgave hentOppgave(String oppgaveId);

    void feilregistrerOppgave(String oppgaveId);

    void ferdigstillOppgave(String oppgaveId);

    List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper);

    List<Oppgave> finnÅpneOppgaverAvTyper(String aktørId, String tema, List<Oppgavetype> oppgaveTyper);

    List<Oppgave> finnÅpneOppgaverForEnhet(String tema, List<String> oppgaveTyper, String tildeltEnhetsnr, String limit);

}

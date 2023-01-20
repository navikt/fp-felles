package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;

public interface Oppgaver {

    Oppgave hentOppgave(String oppgaveId);

    void feilregistrerOppgave(String oppgaveId);

    void ferdigstillOppgave(String oppgaveId);

    List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception;
    List<Oppgave> finnÅpneOppgaverForEnhet(String aktørId, String tema, List<String> oppgaveTyper, String tildeltEnhetsnr) throws Exception;

    List<Oppgave> finnAlleOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception;

    Oppgave opprettetOppgave(OpprettOppgave oppgave);

}

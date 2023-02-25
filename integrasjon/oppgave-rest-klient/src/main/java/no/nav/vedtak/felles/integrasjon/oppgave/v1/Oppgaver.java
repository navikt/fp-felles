package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;

public interface Oppgaver {

    Oppgave opprettetOppgave(OpprettOppgave oppgave);

    Oppgave hentOppgave(String oppgaveId);

    void feilregistrerOppgave(String oppgaveId);

    void ferdigstillOppgave(String oppgaveId);

    List<Oppgave> finnÅpneOppgaver(String aktørId, String tema, List<String> oppgaveTyper);

    // Henter åpne oppgaver med Tema-kode Foreldrepenger for gitt Oppgavetyper.
    // Øvrige argumenter kan være null,  men hvis det ventes mange oppgaver så sett aktør, enhet eller limit
    List<Oppgave> finnÅpneOppgaverAvType(Oppgavetype oppgaveType, String aktørId, String enhetsNr, String limit);

    List<Oppgave> finnÅpneOppgaverForEnhet(String tema, List<String> oppgaveTyper, String enhetsNr, String limit);

    // Henter åpne oppgaver med Tema-kode Foreldrepenger for gitte Opgavetyper-kodeverk (kan være tom).
    // Øvrige argumenter kan være null, men hvis det ventes mange oppgaver så sett aktør, enhet eller limit
    List<Oppgave> finnÅpneOppgaver(List<String> oppgaveTyper, String aktørId, String enhetsNr, String limit);

}

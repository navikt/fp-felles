package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;


public record FinnOppgaveResponse(long antallTreffTotalt, List<Oppgave> oppgaver) {


    @Deprecated
    public long getAntallTreffTotalt() {
        return antallTreffTotalt();
    }

    @Deprecated
    public List<Oppgave> getOppgaver() {
        return oppgaver();
    }


}

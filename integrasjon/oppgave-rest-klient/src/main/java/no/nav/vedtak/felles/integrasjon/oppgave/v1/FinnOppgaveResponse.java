package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;


public record FinnOppgaveResponse(long antallTreffTotalt, List<Oppgave> oppgaver) {


    @Deprecated(since = "4.0.x", forRemoval = true)
    public long getAntallTreffTotalt() {
        return antallTreffTotalt();
    }

    @Deprecated(since = "4.0.x", forRemoval = true)
    public List<Oppgave> getOppgaver() {
        return oppgaver();
    }


}

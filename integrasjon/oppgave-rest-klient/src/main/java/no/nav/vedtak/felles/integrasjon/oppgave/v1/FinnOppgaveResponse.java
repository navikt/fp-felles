package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import java.util.List;


public record FinnOppgaveResponse(long antallTreffTotalt, List<Oppgave> oppgaver) {
}

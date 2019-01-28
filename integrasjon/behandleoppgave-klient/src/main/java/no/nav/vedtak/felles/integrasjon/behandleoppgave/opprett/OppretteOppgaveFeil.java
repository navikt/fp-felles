package no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface OppretteOppgaveFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "F-249284", feilmelding = "Fikk ugyldig oppgaveId fra GSAK", logLevel = LogLevel.ERROR)
    Feil fikkUgyldigResponse();
    
    @ManglerTilgangFeil(feilkode = "F-376291", feilmelding = "Mangler tilgang til å utføre opprettOppgave mot GSAK", logLevel = LogLevel.ERROR)
    Feil opprettOppgaveSikkerhetsbegrensing(WSSikkerhetsbegrensningException e);
}

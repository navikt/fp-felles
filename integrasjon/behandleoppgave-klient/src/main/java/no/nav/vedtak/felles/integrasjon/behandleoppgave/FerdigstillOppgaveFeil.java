package no.nav.vedtak.felles.integrasjon.behandleoppgave;

import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface FerdigstillOppgaveFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "F-249285", feilmelding = "Response fra GSAK ved ferdigstilling av oppgave. feilBeskrivelse=%s", logLevel = LogLevel.ERROR)
    Feil fikkFeilIResponse(String feilBeskrivelse);
    
    @ManglerTilgangFeil(feilkode = "F-560626", feilmelding = "Mangler tilgang til å utføre ferdistilll oppgave mot GSAK", logLevel = LogLevel.ERROR)
    Feil ferdigstillOppgaveSikkerhetsbegrensing(WSSikkerhetsbegrensningException e);
}
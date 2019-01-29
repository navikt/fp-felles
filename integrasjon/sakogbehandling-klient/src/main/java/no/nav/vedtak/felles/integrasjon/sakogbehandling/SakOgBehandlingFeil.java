package no.nav.vedtak.felles.integrasjon.sakogbehandling;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface SakOgBehandlingFeil extends DeklarerteFeil {
    SakOgBehandlingFeil FACTORY = FeilFactory.create(SakOgBehandlingFeil.class);

    @TekniskFeil(feilkode = "F-977765", feilmelding = "Feil ved opprettelse av melding til SakOgBehandling", logLevel = LogLevel.ERROR)
    Feil feilVedOpprettelseAvMeldingTilSakOgBehandling(Exception cause);

    @TekniskFeil(feilkode = "F-549225", feilmelding = "Feil ved å sende status avsluttet behandling til SakOgBehandling", logLevel = LogLevel.ERROR)
    Feil feilVedAvsluttMeldingStatusTilSakOgBehandling(Exception cause);
}
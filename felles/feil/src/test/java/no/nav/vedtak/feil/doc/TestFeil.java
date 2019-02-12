package no.nav.vedtak.feil.doc;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface TestFeil extends DeklarerteFeil {

    @TekniskFeil(feilkode = "F-999999", logLevel = LogLevel.ERROR, feilmelding = "test feil")
    Feil detteErEnFeil(RuntimeException e);

}
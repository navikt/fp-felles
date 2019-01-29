package no.nav.vedtak.felles.integrasjon.jms;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface JmsFeil extends DeklarerteFeil {
    @TekniskFeil(feilkode = "F-620259", feilmelding = "Required property %s not set, or blank.", logLevel = LogLevel.ERROR)
    Feil manglerNødvendigSystemProperty(String navn);

    @TekniskFeil(feilkode = "F-943167", feilmelding = "Property %s not a valid integer.", logLevel = LogLevel.ERROR)
    Feil ikkeIntegerSystemProperty(String navn);
}

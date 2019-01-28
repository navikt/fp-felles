package no.nav.vedtak.sikkerhet.jaspic;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.io.IOException;

interface OidcAuthModuleFeil extends DeklarerteFeil {

    OidcAuthModuleFeil FACTORY = FeilFactory.create(OidcAuthModuleFeil.class);

    @TekniskFeil(feilkode = "F-396795", feilmelding = "Klarte ikke å sende respons", logLevel = LogLevel.WARN)
    Feil klarteIkkeSendeRespons(IOException e);
}

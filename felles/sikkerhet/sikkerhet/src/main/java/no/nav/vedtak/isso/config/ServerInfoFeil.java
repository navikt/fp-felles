package no.nav.vedtak.isso.config;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface ServerInfoFeil extends DeklarerteFeil {
    ServerInfoFeil FACTORY = FeilFactory.create(ServerInfoFeil.class);

    @TekniskFeil(feilkode = "F-720999", feilmelding = "Mangler nødvendig system property '%s'", logLevel = LogLevel.ERROR)
    Feil manglerNødvendigSystemProperty(String key);

    @TekniskFeil(feilkode = "F-836622", feilmelding = "Ugyldig system property '%s'='%s'", logLevel = LogLevel.ERROR)
    Feil ugyldigSystemProperty(String key, String value);

    @TekniskFeil(feilkode = "F-050157", feilmelding = "Uventet format for host, klarer ikke å utvide cookie domain. Forventet format var xx.xx.xx, fikk '%s'. (OK hvis kjører lokalt).", logLevel = LogLevel.WARN)
    Feil uventetHostFormat(String host);
}

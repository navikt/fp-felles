package no.nav.vedtak.isso.ressurs;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.io.UnsupportedEncodingException;

interface RelyingPartyCallbackFeil extends DeklarerteFeil {
    RelyingPartyCallbackFeil FACTORY = FeilFactory.create(RelyingPartyCallbackFeil.class);

    @TekniskFeil(feilkode = "F-963044", feilmelding = "Mangler parameter 'code' i URL", logLevel = LogLevel.WARN)
    Feil manglerCodeParameter();

    @TekniskFeil(feilkode = "F-731807", feilmelding = "Mangler parameter 'state' i URL", logLevel = LogLevel.WARN)
    Feil manglerStateParameter();

    @TekniskFeil(feilkode = "F-755892", feilmelding = "Cookie for redirect URL mangler eller er tom", logLevel = LogLevel.WARN)
    Feil manglerCookieForRedirectionURL();

    @TekniskFeil(feilkode = "F-448219", feilmelding = "Kunne ikke URL decode '%s'", logLevel = LogLevel.WARN)
    Feil kunneIkkeUrlDecode(String urlEncoded, UnsupportedEncodingException e);
}

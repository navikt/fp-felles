package no.nav.vedtak.isso;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface OpenAmFeil extends DeklarerteFeil {

    OpenAmFeil FACTORY = FeilFactory.create(OpenAmFeil.class);

    @TekniskFeil(feilkode = "F-502086", feilmelding = "Uventet feil ved utfylling av authorization template", logLevel = LogLevel.ERROR)
    Feil uventetFeilVedUtfyllingAvAuthorizationTemplate(IOException cause);

    @TekniskFeil(feilkode = "F-945077", feilmelding = "Feil i konfigurert redirect uri: %s", logLevel = LogLevel.ERROR)
    Feil feilIKonfigurertRedirectUri(String redirectBase, UnsupportedEncodingException e);

    @TekniskFeil(feilkode = "F-011609", feilmelding = "Ikke-forventet respons fra OpenAm, statusCode %s og respons '%s'", logLevel = LogLevel.WARN)
    Feil uforventetResponsFraOpenAM(int statusCode, String responseString);

    @TekniskFeil(feilkode = "F-404323", feilmelding = "Kunne ikke parse JSON: '%s'", logLevel = LogLevel.WARN)
    Feil kunneIkkeParseJson(String response, IOException e);

    @TekniskFeil(feilkode = "F-909480", feilmelding = "Fant ikke auth-code på responsen, får respons: '%s - %s'", logLevel = LogLevel.WARN)
    Feil kunneIkkeFinneAuthCode(int statusCode, String reason);
}

package no.nav.vedtak.isso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import no.nav.vedtak.exception.TekniskException;

class OpenAmFeil {
    private OpenAmFeil() {

    }

    static TekniskException uventetFeilVedUtfyllingAvAuthorizationTemplate(IOException e) {
        return new TekniskException("F-502086", "Uventet feil ved utfylling av authorization template", e);
    }

    static TekniskException feilIKonfigurertRedirectUri(String redirectBase, UnsupportedEncodingException e) {
        return new TekniskException("F-945077", String.format("Feil i konfigurert redirect uri: %s", redirectBase), e);
    }

    static TekniskException uforventetResponsFraOpenAM(int statusCode, String responseString) {
        return new TekniskException("F-011609",
                String.format("Ikke-forventet respons fra OpenAm, statusCode %s og respons '%s'", statusCode, responseString));
    }

    static TekniskException kunneIkkeParseJson(String response, IOException e) {
        return new TekniskException("F-404323",
                String.format("Kunne ikke parse JSON: '%s'", response), e);
    }

    static TekniskException kunneIkkeFinneAuthCode(int statusCode, String reason) {
        return new TekniskException("F-909480", String.format("Fant ikke auth-code på responsen, får respons: '%s - %s'", statusCode, reason));
    }

    static TekniskException serviceDiscoveryFailed(String url, IOException e) {
        return new TekniskException("F-312233", String.format("Service Discovery feilet mot well-known host: '%s'", url), e);
    }
}

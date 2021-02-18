package no.nav.vedtak.isso.ressurs;

import java.io.UnsupportedEncodingException;

import no.nav.vedtak.exception.TekniskException;

class RelyingPartyCallbackFeil {

    private RelyingPartyCallbackFeil() {

    }

    static TekniskException manglerCodeParameter() {
        return new TekniskException("F-963044", "Mangler parameter 'code' i URL");
    }

    static TekniskException manglerStateParameter() {
        return new TekniskException("F-731807", "Mangler parameter 'state' i URL");
    }

    static TekniskException manglerCookieForRedirectionURL() {
        return new TekniskException("F-755892", "Cookie for redirect URL mangler eller er tom");
    }

    static TekniskException kunneIkkeUrlDecode(String urlEncoded, UnsupportedEncodingException e) {
        return new TekniskException("F-755448219892", String.format("Kunne ikke URL decode '%s'", urlEncoded), e);
    }
}

package no.nav.vedtak.sikkerhet;

import javax.security.auth.callback.Callback;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

public class TokenCallback implements Callback, java.io.Serializable {

    public OpenIDToken getToken() {
        return token;
    }

    public void setToken(OpenIDToken token) {
        this.token = token;
    }

    private OpenIDToken token;
}

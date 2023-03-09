package no.nav.vedtak.sikkerhet;

import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;

import javax.security.auth.callback.Callback;

public class TokenCallback implements Callback, java.io.Serializable {

    public OpenIDToken getToken() {
        return token;
    }

    public void setToken(OpenIDToken token) {
        this.token = token;
    }

    private OpenIDToken token;
}

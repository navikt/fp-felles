package no.nav.vedtak.isso.ressurs;
import javax.security.auth.callback.Callback;

import no.nav.vedtak.sikkerhet.jaspic.OidcTokenHolder;

public class TokenCallback implements Callback, java.io.Serializable {

    public OidcTokenHolder getToken() {
        return token;
    }

    public void setToken(OidcTokenHolder token) {
        this.token = token;
    }

    private OidcTokenHolder token;
}
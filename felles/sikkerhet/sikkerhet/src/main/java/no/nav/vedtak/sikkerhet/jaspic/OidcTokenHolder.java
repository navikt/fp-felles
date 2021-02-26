package no.nav.vedtak.sikkerhet.jaspic;

import java.util.Objects;

public class OidcTokenHolder {

    private final String token;
    private final boolean fromCookie;

    public OidcTokenHolder(String token, boolean fromCookie) {
        this.token = Objects.requireNonNull(token, "token kan ikke være null");
        this.fromCookie = fromCookie;
    }

    public String getToken() {
        return token;
    }

    public boolean isFromCookie() {
        return fromCookie;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<token=" + maskerOidcToken(token) + ", fromCookie=" + fromCookie + ">";
    }

    private static String maskerOidcToken(String token) {
        return token.substring(0, token.lastIndexOf('.')) + ".MASKERT";
    }
}
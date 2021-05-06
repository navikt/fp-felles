package no.nav.vedtak.sikkerhet.jaspic;

public record OidcTokenHolder(String token, boolean fromCookie) {

    @Deprecated
    public String getToken() {
        return token();
    }

    @Deprecated
    public boolean isFromCookie() {
        return fromCookie();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<token=" + maskerOidcToken(token) + ", fromCookie=" + fromCookie + ">";
    }

    private static String maskerOidcToken(String token) {
        return token.substring(0, token.lastIndexOf('.')) + ".MASKERT";
    }
}
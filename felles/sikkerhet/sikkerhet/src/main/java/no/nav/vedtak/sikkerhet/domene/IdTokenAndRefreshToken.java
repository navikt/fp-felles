package no.nav.vedtak.sikkerhet.domene;

public class IdTokenAndRefreshToken {
    private final OidcCredential idToken;
    private final String refreshToken;

    public IdTokenAndRefreshToken(OidcCredential idToken, String refreshToken) {
        this.idToken = idToken;
        this.refreshToken = refreshToken;
    }

    public OidcCredential getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}

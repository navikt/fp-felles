package no.nav.vedtak.sikkerhet.domene;

public class IdTokenAndRefreshToken {
    private OidcCredential idToken;
    private String refreshToken;

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

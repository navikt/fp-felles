package no.nav.vedtak.sikkerhet.domene;

public record IdTokenAndRefreshToken(OidcCredential idToken, String refreshToken) {
}

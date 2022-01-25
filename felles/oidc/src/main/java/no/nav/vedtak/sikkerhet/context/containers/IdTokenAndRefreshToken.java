package no.nav.vedtak.sikkerhet.context.containers;

public record IdTokenAndRefreshToken(OidcCredential idToken, String refreshToken) {
}

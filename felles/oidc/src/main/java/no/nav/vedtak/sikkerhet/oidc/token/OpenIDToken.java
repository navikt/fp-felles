package no.nav.vedtak.sikkerhet.oidc.token;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public record OpenIDToken(OpenIDProvider provider,
                          String tokenType,
                          String token,
                          String scope,
                          long expiresAtMillis) {

    public static final String OIDC_DEFAULT_TOKEN_TYPE = "Bearer ";

    private static final long MILLIS = 1000L;
    private static final int LONGLIFE = 300;
    private static final int BUFFER = 120;

    public OpenIDToken(String token) {
        this(null, OIDC_DEFAULT_TOKEN_TYPE, token, null, System.currentTimeMillis() + (60 * MILLIS));
    }

    public OpenIDToken(OpenIDProvider provider,
                       String tokenType,
                       String token,
                       String scope,
                       Integer expireIn) {
        this(provider, tokenType, token, scope,
            System.currentTimeMillis() + (MILLIS * expireIn > LONGLIFE ? expireIn - BUFFER : expireIn));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtMillis;
    }

    public LocalDateTime expiresAt() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiresAtMillis), ZoneId.systemDefault());
    }

    public OpenIDToken copy() {
        return new OpenIDToken(provider(), tokenType(), token(), scope(), expiresAtMillis());
    }

    @Override
    public String toString() {
        return "OpenIDToken{" +
            "provider=" + provider +
            ", expiresAt=" + expiresAt() +
            '}';
    }
}

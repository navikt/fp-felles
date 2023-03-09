package no.nav.vedtak.sikkerhet.oidc.token;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

public record OpenIDToken(OpenIDProvider provider,
                          String tokenType,
                          TokenString primary,
                          String scope,
                          long expiresAtMillis) {

    public static final String OIDC_DEFAULT_TOKEN_TYPE = "Bearer ";

    private static final long MILLIS = 1000L;
    private static final int LONGLIFE = 300;
    private static final int BUFFER = 120;

    public OpenIDToken(OpenIDProvider provider, TokenString token) {
        this(provider, OIDC_DEFAULT_TOKEN_TYPE, token, null, System.currentTimeMillis() + (150 * MILLIS));
    }

    public OpenIDToken(OpenIDProvider provider,
                       String tokenType,
                       TokenString primary,
                       String scope,
                       Integer expireIn) {
        this(provider, tokenType, primary, scope, expireAtFromExpireIn(expireIn));
    }

    public boolean isNotExpired() {
        return System.currentTimeMillis() < expiresAtMillis;
    }

    public LocalDateTime expiresAt() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiresAtMillis), ZoneId.systemDefault());
    }

    public OpenIDToken copy() {
        return new OpenIDToken(provider(), tokenType(), primary(), scope(), expiresAtMillis());
    }

    public String token() {
        return primary().token();
    }

    @Override
    public String toString() {
        return "OpenIDToken{" +
            "provider=" + provider +
            ", expiresAt=" + expiresAt() +
            '}';
    }

    private static long expireAtFromExpireIn(Integer expireIn) {
        return System.currentTimeMillis() + (MILLIS * (Optional.ofNullable(expireIn).map(e -> e > LONGLIFE ? expireIn - BUFFER : expireIn).orElse(0)));
    }
}

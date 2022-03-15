package no.nav.vedtak.sikkerhet.oidc.token;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;

public record OpenIDToken(OpenIDProvider provider,
                          String tokenType,
                          TokenString primary,
                          String scope,
                          TokenString refresh,
                          long expiresAtMillis) {

    public static final String OIDC_DEFAULT_TOKEN_TYPE = "Bearer ";

    private static final long MILLIS = 1000L;
    private static final int LONGLIFE = 300;
    private static final int BUFFER = 120;

    public OpenIDToken(OpenIDProvider provider, TokenString token) {
        this(provider, OIDC_DEFAULT_TOKEN_TYPE, token, null, null,System.currentTimeMillis() + (150 * MILLIS));
    }

    public OpenIDToken(OpenIDProvider provider,
                       String tokenType,
                       TokenString primary,
                       String scope,
                       Integer expireIn) {
        this(provider, tokenType, primary, scope, null, expireIn);
    }

    public OpenIDToken(OpenIDProvider provider,
                       String tokenType,
                       TokenString primary,
                       String scope,
                       TokenString refresh,
                       Integer expireIn) {
        this(provider, tokenType, primary, scope, refresh,
            System.currentTimeMillis() + (MILLIS * (expireIn > LONGLIFE ? expireIn - BUFFER : expireIn)));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtMillis;
    }

    public LocalDateTime expiresAt() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(expiresAtMillis), ZoneId.systemDefault());
    }

    public OpenIDToken copy() {
        return new OpenIDToken(provider(), tokenType(), primary(), scope(), this.refresh(), expiresAtMillis());
    }

    public String token() {
        return primary().token();
    }

    public Optional<String> refreshToken() {
        return Optional.ofNullable(this.refresh()).map(TokenString::token);
    }

    @Override
    public String toString() {
        return "OpenIDToken{" +
            "provider=" + provider +
            ", expiresAt=" + expiresAt() +
            '}';
    }
}

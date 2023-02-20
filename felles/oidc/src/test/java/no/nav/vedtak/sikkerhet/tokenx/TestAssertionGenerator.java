package no.nav.vedtak.sikkerhet.tokenx;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;

import org.jose4j.jwt.MalformedClaimException;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.oidc.validator.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.validator.KeyStoreTool;

public class TestAssertionGenerator {

    @Test
    public void lag_signer_valider_tokenx_assertion() throws MalformedClaimException {
        var generator = new TokenXAssertionGenerator(URI.create("https://token/endpoint"), "minKlient", KeyStoreTool.getJsonWebKey());
        var token = generator.assertion();

        var claims = JwtUtil.getClaims(token);
        assertThat(claims.getClaimValueAsString("iss")).isEqualTo("minKlient");
        assertThat(claims.getClaimValueAsString("sub")).isEqualTo("minKlient");
        assertThat(claims.getClaimValueAsString("aud").toString()).contains("https://token/endpoint");
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("nbf", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("iat", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("exp", Long.class))).isAfter(Instant.now());

    }

}

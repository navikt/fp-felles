package no.nav.vedtak.sikkerhet.oidc.token.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.jose4j.jwt.MalformedClaimException;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.oidc.validator.JwtUtil;
import no.nav.vedtak.sikkerhet.oidc.validator.KeyStoreTool;

public class TestMaskinportenAssertionGenerator {

    @Test
    void lag_signer_valider_maskinporten_assertion_med_ressurs() throws MalformedClaimException {
        var generator = new MaskinportenAssertionGenerator("minKlient", "https://test.maskinporten.no/", KeyStoreTool.getJsonWebKey());
        var token = generator.assertion("mitt:scope:delvis", "https://beskyttet-ressurs.no/sti");

        var claims = JwtUtil.getClaims(token);
        assertThat(claims.getClaimValueAsString("iss")).isEqualTo("minKlient");
        assertThat(claims.getClaimValueAsString("aud")).contains("https://test.maskinporten.no/");
        assertThat(claims.getClaimValueAsString("scope")).contains("mitt:scope:delvis");
        assertThat(claims.getClaimValueAsString("resource")).contains("https://beskyttet-ressurs.no/sti");
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("nbf", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("iat", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("exp", Long.class))).isAfter(Instant.now());

    }

    @Test
    void lag_signer_valider_maskinporten_assertion_uten_ressurs() throws MalformedClaimException {
        var generator = new MaskinportenAssertionGenerator("minKlient", "https://test.maskinporten.no/", KeyStoreTool.getJsonWebKey());
        var token = generator.assertion("mitt:scope:delvis", null);

        var claims = JwtUtil.getClaims(token);
        assertThat(claims.getClaimValueAsString("iss")).isEqualTo("minKlient");
        assertThat(claims.getClaimValueAsString("aud")).contains("https://test.maskinporten.no/");
        assertThat(claims.getClaimValueAsString("scope")).contains("mitt:scope:delvis");
        assertThat(claims.getClaimValueAsString("resource")).isNull();
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("nbf", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("iat", Long.class))).isBefore(Instant.now());
        assertThat(Instant.ofEpochSecond(claims.getClaimValue("exp", Long.class))).isAfter(Instant.now());

    }

}

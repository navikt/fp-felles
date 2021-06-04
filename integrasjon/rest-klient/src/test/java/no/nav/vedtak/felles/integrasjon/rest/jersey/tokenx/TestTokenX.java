package no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.integrasjon.rest.jersey.tokenx.TokenXAudienceGenerator;

class TestTokenX {

    @Test
    void testAudience() {
        System.setProperty("pdl-api.default", "dev-fss");
        var uri = URI.create("http://pdl-api.default/graphql");
        var aud = new TokenXAudienceGenerator().audience(uri);
        assertEquals("dev-fss:default:pdl-api", aud.asAudience());
    }

}

package no.nav.vedtak.sikkerhet.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.security.mock.oauth2.MockOAuth2Server;

public class WellKnownConfigurationHelperTest {

    private MockOAuth2Server server;

    @BeforeEach
    public void setUp() {
        server = new MockOAuth2Server();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @AfterEach
    public void tearDown() {
        try {
            server.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void skal_returnere_data_fra_well_knows() {
        var issuerId = "default";
        var wellKnownUrl = server.wellKnownUrl(issuerId).toString();

        assertThat(WellKnownConfigurationHelper.getIssuerFra(wellKnownUrl).orElse(null)).isEqualTo(server.baseUrl() + issuerId);
        assertThat(WellKnownConfigurationHelper.getJwksFra(wellKnownUrl).orElse(null)).isEqualTo(server.baseUrl() + issuerId + "/jwks");
    }

    @Test
    public void skal_kaste_illegal_argument_exception() {
        var exception = assertThrows(IllegalArgumentException.class,
            () -> WellKnownConfigurationHelper.getIssuerFra("ukjent_url"));

        assertThat(exception.getMessage()).contains("URI is not absolute");
    }

    @Test
    public void skal_returnere_empty_optional() {
        assertThat(WellKnownConfigurationHelper.getIssuerFra(null)).isEmpty();
    }

}

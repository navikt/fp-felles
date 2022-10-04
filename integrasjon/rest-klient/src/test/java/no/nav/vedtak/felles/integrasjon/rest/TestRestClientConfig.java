package no.nav.vedtak.felles.integrasjon.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class TestRestClientConfig {

    @Test
    void testCase_app_med_endpoint_property_uten_verdi() {
        var config = RestConfig.forClient(TestAbakusMedEndpointProperty.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.CONTEXT);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPABAKUS));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
    }

    @Test
    void testCase_app_med_endpoint_property_som_har_verdi() {
        System.setProperty("non.existent", "http://fpabakus/fpabakus/ekstern/target");
        var config = RestConfig.forClient(TestAbakusMedEndpointProperty.class);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPABAKUS));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpabakus/fpabakus/ekstern/target"));
        System.clearProperty("non.existent");
    }

    @Test
    void testCase_kun_application_uten_properties() {
        var config = RestConfig.forClient(TestRiskUtenEndpoint.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.STS_CC);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPRISK));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
    }

    @Test
    void testCase_app_med_scope_property_uten_verdi() {
        var config = RestConfig.forClient(TestFormidlingAdaptiveMedScopeProperty.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.ADAPTIVE);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPFORMIDLING));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
    }

    @Test
    void testCase_app_med_scope_property_med_verdi() {
        System.setProperty("non.existent", "api://local.default.fpformidling-local/.default");
        var config = RestConfig.forClient(TestFormidlingAdaptiveMedScopeProperty.class);
        assertThat(config.scopes()).isEqualTo("api://local.default.fpformidling-local/.default");
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        System.clearProperty("non.existent");
    }

    @Test
    void testCase_ikke_app_med_endpoint_scope_property_uten_verdi() {
        var config = RestConfig.forClient(TestFormidlingMedEndpointScopes.class);
        assertThat(config.scopes()).isEqualTo("api://local.teamforeldrepenger.fp-formidling/.default");
        assertThat(config.getContextPath()).isEmpty();
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpformidling/fpformidling/default"));
    }

    @Test
    void testCase_ikke_app_med_endpoint_scope_property_med_verdi() {
        System.setProperty("non.existent", "http://fpformidling/fpformidling/ekstern/target");
        System.setProperty("non.existent2", "api://local.default.fpformidling/.default");
        var config = RestConfig.forClient(TestFormidlingMedEndpointScopes.class);
        assertThat(config.scopes()).isEqualTo("api://local.default.fpformidling/.default");
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpformidling/fpformidling/ekstern/target"));
        System.clearProperty("non.existent");
        System.clearProperty("non.existent2");
    }

    @RestClientConfig(application = FpApplication.FPABAKUS, endpointProperty = "non.existent", endpointDefault = "http://fpabakus/fpabakus/ekstern")
    private static class TestAbakusMedEndpointProperty {

    }

    @RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPRISK)
    private static class TestRiskUtenEndpoint {

    }

    @RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPFORMIDLING, scopesProperty = "non.existent", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestFormidlingAdaptiveMedScopeProperty {

    }

    @RestClientConfig(endpointProperty = "non.existent", endpointDefault = "http://fpformidling/fpformidling/default",
        scopesProperty = "non.existent2", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestFormidlingMedEndpointScopes {

    }



}

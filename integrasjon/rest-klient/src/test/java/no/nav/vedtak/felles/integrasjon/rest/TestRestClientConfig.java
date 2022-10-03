package no.nav.vedtak.felles.integrasjon.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class TestRestClientConfig {

    @Test
    void testCaseA() {
        var config = RestConfig.forClient(TestA.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.CONTEXT);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPABAKUS));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
    }

    @Test
    void testCaseA1() {
        System.setProperty("non.existent", "http://fpabakus/fpabakus/ekstern/target");
        var config = RestConfig.forClient(TestA.class);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPABAKUS));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpabakus/fpabakus/ekstern/target"));
        System.clearProperty("non.existent");
    }

    @Test
    void testCaseB() {
        var config = RestConfig.forClient(TestB.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.STS_CC);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPRISK));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
    }

    @Test
    void testCaseC() {
        var config = RestConfig.forClient(TestC.class);
        assertThat(config.tokenConfig()).isEqualTo(TokenFlow.ADAPTIVE);
        assertThat(config.scopes()).isEqualTo(FpApplication.scopesFor(FpApplication.FPFORMIDLING));
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
    }

    @Test
    void testCaseC1() {
        System.setProperty("non.existent", "api://local.default.fpformidling-local/.default");
        var config = RestConfig.forClient(TestC.class);
        assertThat(config.scopes()).isEqualTo("api://local.default.fpformidling-local/.default");
        assertThat(config.fpContextPath()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(config.endpoint()).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        System.clearProperty("non.existent");
    }

    @Test
    void testCaseD() {
        var config = RestConfig.forClient(TestD.class);
        assertThat(config.scopes()).isEqualTo("api://local.teamforeldrepenger.fp-formidling/.default");
        assertThat(config.getContextPath()).isEmpty();
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpformidling/fpformidling/default"));
    }

    @Test
    void testCaseD1() {
        System.setProperty("non.existent", "http://fpformidling/fpformidling/ekstern/target");
        System.setProperty("non.existent2", "api://local.default.fpformidling/.default");
        var config = RestConfig.forClient(TestD.class);
        assertThat(config.scopes()).isEqualTo("api://local.default.fpformidling/.default");
        assertThat(config.endpoint()).isEqualTo(URI.create("http://fpformidling/fpformidling/ekstern/target"));
        System.clearProperty("non.existent");
        System.clearProperty("non.existent2");
    }

    @RestClientConfig(application = FpApplication.FPABAKUS, endpointProperty = "non.existent", endpointDefault = "http://fpabakus/fpabakus/ekstern")
    private static class TestA {

    }

    @RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPRISK)
    private static class TestB {

    }

    @RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPFORMIDLING, scopesProperty = "non.existent", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestC {

    }

    @RestClientConfig(endpointProperty = "non.existent", endpointDefault = "http://fpformidling/fpformidling/default",
        scopesProperty = "non.existent2", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestD {

    }



}

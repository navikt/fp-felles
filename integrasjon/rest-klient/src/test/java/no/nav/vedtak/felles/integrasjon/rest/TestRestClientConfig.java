package no.nav.vedtak.felles.integrasjon.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;

public class TestRestClientConfig {

    @Test
    void testCaseA() {
        assertThat(RestConfig.applicationFromAnnotation(TestA.class)).hasValueSatisfying(v -> assertThat(FpApplication.FPABAKUS).isEqualTo(v));
        assertThat(RestConfig.scopesFromAnnotation(TestA.class)).isEqualTo("api://local.default.fpabakus/.default");
        assertThat(RestConfig.contextPathFromAnnotation(TestA.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(RestConfig.endpointFromAnnotation(TestA.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
    }

    @Test
    void testCaseA1() {
        System.setProperty("non.existent", "http://fpabakus/fpabakus/ekstern/target");
        assertThat(RestConfig.applicationFromAnnotation(TestA.class)).hasValueSatisfying(v -> assertThat(FpApplication.FPABAKUS).isEqualTo(v));
        assertThat(RestConfig.scopesFromAnnotation(TestA.class)).isEqualTo(FpApplication.scopesFor(FpApplication.FPABAKUS));
        assertThat(RestConfig.contextPathFromAnnotation(TestA.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPABAKUS)));
        assertThat(RestConfig.endpointFromAnnotation(TestA.class)).isEqualTo(URI.create("http://fpabakus/fpabakus/ekstern/target"));
        System.clearProperty("non.existent");
    }

    @Test
    void testCaseB() {
        assertThat(RestConfig.applicationFromAnnotation(TestB.class)).hasValueSatisfying(v -> assertThat(FpApplication.FPRISK).isEqualTo(v));
        assertThat(RestConfig.scopesFromAnnotation(TestB.class)).isEqualTo(FpApplication.scopesFor(FpApplication.FPRISK));
        assertThat(RestConfig.contextPathFromAnnotation(TestB.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
        assertThat(RestConfig.endpointFromAnnotation(TestB.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPRISK)));
    }

    @Test
    void testCaseC() {
        assertThat(RestConfig.applicationFromAnnotation(TestC.class)).hasValueSatisfying(v -> assertThat(FpApplication.FPFORMIDLING).isEqualTo(v));
        assertThat(RestConfig.scopesFromAnnotation(TestC.class)).isEqualTo(FpApplication.scopesFor(FpApplication.FPFORMIDLING));
        assertThat(RestConfig.contextPathFromAnnotation(TestC.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(RestConfig.endpointFromAnnotation(TestC.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
    }

    @Test
    void testCaseC1() {
        System.setProperty("non.existent", "api://local.default.fpformidling-local/.default");
        assertThat(RestConfig.applicationFromAnnotation(TestC.class)).hasValueSatisfying(v -> assertThat(FpApplication.FPFORMIDLING).isEqualTo(v));
        assertThat(RestConfig.scopesFromAnnotation(TestC.class)).isEqualTo("api://local.default.fpformidling-local/.default");
        assertThat(RestConfig.contextPathFromAnnotation(TestC.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        assertThat(RestConfig.endpointFromAnnotation(TestC.class)).isEqualTo(URI.create(FpApplication.contextPathFor(FpApplication.FPFORMIDLING)));
        System.clearProperty("non.existent");
    }

    @Test
    void testCaseD() {
        assertThat(RestConfig.applicationFromAnnotation(TestD.class)).isEmpty();
        assertThat(RestConfig.scopesFromAnnotation(TestD.class)).isEqualTo("api://local.teamforeldrepenger.fp-formidling/.default");
        assertThrows(IllegalArgumentException.class, () -> RestConfig.contextPathFromAnnotation(TestD.class));
        assertThat(RestConfig.endpointFromAnnotation(TestD.class)).isEqualTo(URI.create("http://fpformidling/fpformidling/default"));
    }

    @Test
    void testCaseD1() {
        System.setProperty("non.existent", "http://fpformidling/fpformidling/ekstern/target");
        System.setProperty("non.existent2", "api://local.default.fpformidling/.default");
        assertThat(RestConfig.scopesFromAnnotation(TestD.class)).isEqualTo("api://local.default.fpformidling/.default");
        assertThat(RestConfig.endpointFromAnnotation(TestD.class)).isEqualTo(URI.create("http://fpformidling/fpformidling/ekstern/target"));
        System.clearProperty("non.existent");
        System.clearProperty("non.existent2");
    }

    @RestClientConfig(application = FpApplication.FPABAKUS, endpointProperty = "non.existent", endpointDefault = "http://fpabakus/fpabakus/ekstern")
    private static class TestA {

    }

    @RestClientConfig(application = FpApplication.FPRISK)
    private static class TestB {

    }

    @RestClientConfig(application = FpApplication.FPFORMIDLING, scopesProperty = "non.existent", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestC {

    }

    @RestClientConfig(endpointProperty = "non.existent", endpointDefault = "http://fpformidling/fpformidling/default",
        scopesProperty = "non.existent2", scopesDefault = "api://local.teamforeldrepenger.fp-formidling/.default")
    private static class TestD {

    }



}

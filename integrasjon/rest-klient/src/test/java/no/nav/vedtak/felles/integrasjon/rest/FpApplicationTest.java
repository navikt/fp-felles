package no.nav.vedtak.felles.integrasjon.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;

class FpApplicationTest {

    private final Environment environment = Mockito.mock(Environment.class);

    @Test
    void test_at_service_discovery_brukes_mellom_FPAppp_i_samme_cluster() {
        when(environment.getCluster()).thenReturn(Cluster.DEV_FSS);
        when(environment.isDev()).thenReturn(true);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPSAK, environment);
        assertThat(contextPath).isEqualTo("http://fpsak/fpsak");
    }

    @Test
    void test_at_fss_til_gcp_bruker_full_ingress() {
        when(environment.getCluster()).thenReturn(Cluster.DEV_FSS);
        when(environment.isFss()).thenReturn(true);
        when(environment.isDev()).thenReturn(true);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPOVERSIKT, environment);
        assertThat(contextPath).isEqualTo("https://fpoversikt.intern.dev.nav.no/fpoversikt");
    }

    @Test
    void test_at_gcp_til_annen_app_enn_fpsak_i_fss_bruker_ingress_av_type_pub() {
        when(environment.getCluster()).thenReturn(Cluster.DEV_GCP);
        when(environment.isDev()).thenReturn(true);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPTILBAKE, environment);
        assertThat(contextPath).isEqualTo("https://fptilbake.dev-fss-pub.nais.io/fptilbake");
    }

    @Test
    void test_at_gcp_til_fpsak_i_fss_bruker_ingress_av_type_pub() {
        when(environment.getCluster()).thenReturn(Cluster.PROD_GCP);
        when(environment.isProd()).thenReturn(true);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPSAK, environment);
        assertThat(contextPath).isEqualTo("https://fpsak-api.prod-fss-pub.nais.io/fpsak");
    }

    @Test
    void test_at_vi_bruker_default_lokalhost_ved_lokal_kjøring_uten_override_url_satt() {
        when(environment.getCluster()).thenReturn(Cluster.VTP);
        when(environment.isLocal()).thenReturn(true);
        when(environment.getProperty("fpabakus.override.url")).thenReturn(null);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, environment);
        assertThat(contextPath).isEqualTo("http://localhost:8015/fpabakus");
    }

    @Test
    void bruk_override_url_hvis_oppgitt_og_cluster_er_vtp() {
        var overrideUrl = "http://override.url/";
        when(environment.getCluster()).thenReturn(Cluster.VTP);
        when(environment.isLocal()).thenReturn(true);
        when(environment.getProperty("fpabakus.override.url")).thenReturn(overrideUrl);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, environment);
        assertThat(contextPath).isEqualTo(overrideUrl);
    }

    @Test
    void ikke_bruk_override_url_hvis_oppgitt_og_cluster_dev_eller_prod() {
        var overrideUrl = "http://override.url/";
        when(environment.getCluster()).thenReturn(Cluster.DEV_FSS);
        when(environment.isDev()).thenReturn(true);
        when(environment.getProperty("fpabakus.override.url")).thenReturn(overrideUrl);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, environment);
        assertThat(contextPath).isEqualTo("http://fpabakus/fpabakus");
    }
}

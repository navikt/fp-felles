package no.nav.vedtak.felles.integrasjon.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.konfig.Cluster;

class FpApplicationTest {

    @Test
    void test_at_service_discovery_brukes_mellom_FPAppp_i_samme_cluster() {
        var contextPath = FpApplication.contextPathFor(FpApplication.FPSAK, Cluster.DEV_FSS);
        assertThat(contextPath).isEqualTo("http://fpsak/fpsak");
    }

    @Test
    void test_at_fss_til_gcp_bruker_full_ingress() {
        var contextPath = FpApplication.contextPathFor(FpApplication.FPOVERSIKT, Cluster.DEV_FSS);
        assertThat(contextPath).isEqualTo("https://fpoversikt.intern.dev.nav.no/fpoversikt");
    }

    @Test
    void test_at_gcp_til_annen_app_enn_fpsak_i_fss_bruker_ingress_av_type_pub() {
        var contextPath = FpApplication.contextPathFor(FpApplication.FPTILBAKE, Cluster.DEV_GCP);
        assertThat(contextPath).isEqualTo("https://fptilbake.dev-fss-pub.nais.io/fptilbake");
    }

    @Test
    void test_at_gcp_til_fpsak_i_fss_bruker_ingress_av_type_pub() {
        var contextPath = FpApplication.contextPathFor(FpApplication.FPSAK, Cluster.DEV_GCP);
        assertThat(contextPath).isEqualTo("https://fpsak-api.dev-fss-pub.nais.io/fpsak");
    }

    @Test
    void test_at_vi_bruker_default_lokalhost_ved_lokal_kjøring_uten_override_url_satt() {
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, Cluster.VTP);
        assertThat(contextPath).isEqualTo("http://localhost:8015/fpabakus");
    }

    @Test
    void bruk_override_url_hvis_oppgitt_og_cluster_er_vtp() {
        var overrideUrl = "http://override.url/";
        System.setProperty("fpabakus.override.url", overrideUrl);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, Cluster.VTP);
        assertThat(contextPath).isEqualTo(overrideUrl);
    }

    @Test
    void ikke_bruk_override_url_hvis_oppgitt_og_cluster_dev_eller_prod() {
        var overrideUrl = "http://override.url/";
        System.setProperty("fpabakus.override.url", overrideUrl);
        var contextPath = FpApplication.contextPathFor(FpApplication.FPABAKUS, Cluster.DEV_FSS);
        assertThat(contextPath).isEqualTo("http://fpabakus/fpabakus");
    }
}

package no.nav.vedtak.sikkerhet.abac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.util.AppLoggerFactory;

public class AbacSporingsloggTest {

    private static MemoryAppender appender;
    private static Logger LOG;

    @BeforeAll
    public static void beforeAll() {
        LOG = Logger.class.cast(AppLoggerFactory.getSporingLogger(DefaultAbacSporingslogg.class));
        LOG.setLevel(Level.INFO);
        appender = new MemoryAppender(LOG.getName());
    }

    @BeforeEach
    public void beforeEach() {
        LOG.addAppender(appender);
        appender.start();
    }

    @AfterEach
    public void afterEach() {
        appender.stop();
        LOG.detachAppender(appender);
    }

    @Test
    public void skal_logge_fra_attributter() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001"));

        sporing.loggTilgang(r, attributter);
        assertNotNull(appender.search("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 saksnummer=SNR0001 ", Level.INFO));
    }

    @Test
    public void skal_lage_flere_rader_når_en_attributt_har_flere_verdier() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "A")
                .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "B")
                .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "C")
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L));

        sporing.loggTilgang(r, attributter);
        assertEquals(3, appender.countEventsForLogger());
        assertNotNull(appender.searchInfo(
                "action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=A behandlingId=1234 saksnummer=SNR0001 "));
        assertNotNull(appender.searchInfo(
                "action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=B behandlingId=1234 saksnummer=SNR0001 "));
        assertNotNull(appender.searchInfo(
                "action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=C behandlingId=1234 saksnummer=SNR0001 "));
    }

    @Test
    public void skal_lage_kryssprodukt_når_det_er_noen_attributter_som_har_flere_verdier() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001")
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0002")
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0003")
                .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "A")
                .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "B"));

        sporing.loggTilgang(r, attributter);

        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0001 "));
        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0002 "));
        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0003 "));
        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0001 "));
        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0002 "));
        assertNotNull(appender.searchInfo("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0003 "));
        // assertThat(sniffer.countEntries("action")).isEqualTo(6);
    }

    @Test
    public void skal_logge_fra_pdp_request() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        sporing.loggTilgang(r, attributter);
        assertNotNull(appender.searchInfo("action=foobar abac_action=null abac_resource_type=null fnr=11111111111"));
        // assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
                .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001"));

        sporing.loggTilgang(r, attributter);

        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111 saksnummer=SNR0001 "));
        // assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_en_rad_fra_pdp_request_og_flere_fra_attributer() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1235L)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1236L));

        sporing.loggTilgang(r, attributter);

        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111"));
        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1235 fnr=11111111111"));
        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1236 fnr=11111111111"));
        // assertThat(sniffer.countEntries("action")).isEqualTo(3);

    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_fler_rader_fra_pdp_request_og_en_fra_attributer() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222", "33333333333")));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L));

        sporing.loggTilgang(r, attributter);

        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111"));
        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=22222222222"));
        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=33333333333"));
        // assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_ha_separate_rader_for_pdpRequest_og_attributter_når_det_er_flere_fra_hver_for_å_unngå_stort_kryssprodukt() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222")));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1235L)
                .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1236L));

        sporing.loggTilgang(r, attributter);

        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null fnr=11111111111 "));
        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null fnr=22222222222 "));
        assertNotNull(
                appender.searchInfo("action=foobar behandlingId=1234"));
        assertNotNull(
                appender.searchInfo("action=foobar behandlingId=1235"));
        assertNotNull(
                appender.searchInfo("action=foobar behandlingId=1236"));
        // assertThat(sniffer.countEntries("action")).isEqualTo(5);

    }

    @Test
    public void skal_erstatte_mellomrom_med_underscore_for_å_forenkle_parsing_av_sporingslogg() throws Exception {
        DefaultAbacSporingslogg sporing = new DefaultAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR 0001"));

        sporing.loggTilgang(r, attributter);

        assertNotNull(
                appender.searchInfo("saksnummer=SNR_0001"));
        // assertThat(sniffer.countEntries("action")).isEqualTo(1);

    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter_ved_deny() throws Exception {
        var sporing = new DefaultAbacSporingslogg();

        var pdpRequest = new PdpRequest();
        pdpRequest.put(NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L));

        sporing.loggDeny(pdpRequest, Collections.singletonList(Decision.Deny), attributter);

        assertNotNull(
                appender.searchInfo("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 decision=Deny fnr=11111111111 "));
        // assertThat(sniffer.countEntries("action")).isEqualTo(1);

    }

}

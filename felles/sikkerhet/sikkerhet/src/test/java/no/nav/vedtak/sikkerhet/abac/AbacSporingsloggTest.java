package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.AKSJONSPUNKT_KODE;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.BEHANDLING_ID;
import static no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType.SAKSNUMMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.vedtak.log.util.MemoryAppender;
import no.nav.vedtak.util.AppLoggerFactory;

public class AbacSporingsloggTest {

    private static MemoryAppender logSniffer;
    private static DefaultAbacSporingslogg sporing;
    private static Logger LOG;

    @BeforeAll
    public static void beforeAll() {
        sporing = new DefaultAbacSporingslogg();
        LOG = Logger.class.cast(AppLoggerFactory.getSporingLogger(DefaultAbacSporingslogg.class));
        LOG.setLevel(Level.INFO);
        logSniffer = new MemoryAppender(LOG.getName());
        LOG.addAppender(logSniffer);
        logSniffer.start();
    }

    @AfterEach
    public void afterEach() {
        logSniffer.reset();
    }

    @Test
    public void skal_logge_fra_attributter() throws Exception {

        var attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token")
                .setAction("foobar").leggTil(AbacDataAttributter.opprett()
                        .leggTil(BEHANDLING_ID, 1234L)
                        .leggTil(SAKSNUMMER, "SNR0001"));

        sporing.loggTilgang(attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 saksnummer=SNR0001 ");
    }

    @Test
    public void skal_lage_flere_rader_når_en_attributt_har_flere_verdier() throws Exception {
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(AKSJONSPUNKT_KODE, "A")
                        .leggTil(AKSJONSPUNKT_KODE, "B")
                        .leggTil(AKSJONSPUNKT_KODE, "C")
                        .leggTil(BEHANDLING_ID, 1234L));

        sporing.loggTilgang(attributter);
        assertEquals(3, logSniffer.countEventsForLogger());
        assertLogged("action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=A behandlingId=1234 saksnummer=SNR0001 ");
        assertLogged("action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=B behandlingId=1234 saksnummer=SNR0001 ");
        assertLogged("action=foobar abac_action=null abac_resource_type=null aksjonspunktKode=C behandlingId=1234 saksnummer=SNR0001 ");
    }

    @Test
    public void skal_lage_kryssprodukt_når_det_er_noen_attributter_som_har_flere_verdier() throws Exception {
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(SAKSNUMMER, "SNR0001")
                        .leggTil(SAKSNUMMER, "SNR0002")
                        .leggTil(SAKSNUMMER, "SNR0003")
                        .leggTil(AKSJONSPUNKT_KODE, "A")
                        .leggTil(AKSJONSPUNKT_KODE, "B"));

        sporing.loggTilgang(new PdpRequest(), attributter);

        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0001 ");
        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0002 ");
        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=B saksnummer=SNR0003 ");
        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0001 ");
        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0002 ");
        assertLogged("foobar abac_action=null abac_resource_type=null aksjonspunktKode=A saksnummer=SNR0003 ");
        assertCount("action", 6);
    }

    @Test
    public void skal_logge_fra_pdp_request() throws Exception {
        var r = new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, List.of("11111111111")));
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar");
        sporing.loggTilgang(r, attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null fnr=11111111111");
        assertCount("action", 1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter() throws Exception {
        var r = new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, List.of("11111111111")));
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(BEHANDLING_ID, 1234L)
                        .leggTil(SAKSNUMMER, "SNR0001"));

        sporing.loggTilgang(r, attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111 saksnummer=SNR0001 ");
        assertCount("action", 1);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_en_rad_fra_pdp_request_og_flere_fra_attributer() throws Exception {
        var r = new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111")));
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(BEHANDLING_ID, 1234L)
                        .leggTil(BEHANDLING_ID, 1235L)
                        .leggTil(BEHANDLING_ID, 1236L));

        sporing.loggTilgang(r, attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111");
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1235 fnr=11111111111");
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1236 fnr=11111111111");
        assertCount("action", 3);

    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_fler_rader_fra_pdp_request_og_en_fra_attributer() throws Exception {
        var r = new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222", "33333333333"))));
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett().leggTil(BEHANDLING_ID, 1234L));
        sporing.loggTilgang(r, attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=11111111111");
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=22222222222");
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 fnr=33333333333");
        assertCount("action", 3);
    }

    @Test
    public void skal_ha_separate_rader_for_pdpRequest_og_attributter_når_det_er_flere_fra_hver_for_å_unngå_stort_kryssprodukt() throws Exception {
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(BEHANDLING_ID, 1234L)
                        .leggTil(BEHANDLING_ID, 1235L)
                        .leggTil(BEHANDLING_ID, 1236L));

        sporing.loggTilgang(
                new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222")))), attributter);

        assertLogged("action=foobar abac_action=null abac_resource_type=null fnr=11111111111 ");
        assertLogged("action=foobar abac_action=null abac_resource_type=null fnr=22222222222 ");
        assertLogged("action=foobar behandlingId=1234");
        assertLogged("action=foobar behandlingId=1235");
        assertLogged("action=foobar behandlingId=1236");
        assertCount("action", 5);
    }

    @Test
    public void skal_erstatte_mellomrom_med_underscore_for_å_forenkle_parsing_av_sporingslogg() throws Exception {

        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett()
                        .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR 0001"));
        sporing.loggTilgang(attributter);

        assertLogged("saksnummer=SNR_0001");
        assertCount("action", 1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter_ved_deny() throws Exception {

        var pdpRequest = new PdpRequest(Map.of(RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111")));
        var attributter = AbacAttributtSamling
                .medJwtToken("dummy.oidc.token")
                .setAction("foobar")
                .leggTil(AbacDataAttributter.opprett().leggTil(BEHANDLING_ID, 1234L));
        sporing.loggDeny(pdpRequest, List.of(Decision.Deny), attributter);
        assertLogged("action=foobar abac_action=null abac_resource_type=null behandlingId=1234 decision=Deny fnr=11111111111 ");
        assertCount("action", 1);
    }

    private static void assertCount(String substring, int n) {
        assertThat(logSniffer.countEntries(substring)).isEqualTo(n);
    }

    private static void assertLogged(String string) {
        assertThat(logSniffer.searchInfo(string)).isNotNull();
    }
}

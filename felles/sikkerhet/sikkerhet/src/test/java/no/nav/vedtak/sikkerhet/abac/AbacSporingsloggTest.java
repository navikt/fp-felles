package no.nav.vedtak.sikkerhet.abac;

import static no.nav.abac.foreldrepenger.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;

import no.nav.abac.common.xacml.CommonAttributter;
import no.nav.modig.core.test.LogSniffer;

public class AbacSporingsloggTest {

    @Rule
    public LogSniffer sniffer = new LogSniffer();

    @Test
    public void skal_logge_fra_attributter() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
            .leggTil(StandardAbacAttributtType.DOKUMENT_DATA_ID, 1000L)
            .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "999999999")
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 behandlingId=1234 dokumentDataId=1000 saksnummer=SNR0001 ");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_lage_flere_rader_når_en_attributt_har_flere_verdier() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "777777777")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "888888888")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "999999999")
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunktId=777777777 behandlingId=1234");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunktId=888888888 behandlingId=1234");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 behandlingId=1234");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_lage_kryssprodukt_når_det_er_noen_attributter_som_har_flere_verdier() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001")
            .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0002")
            .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0003")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "888888888")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "999999999")
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 saksnummer=SNR0001 ");
        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 saksnummer=SNR0002 ");
        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 saksnummer=SNR0003 ");
        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=888888888 saksnummer=SNR0001 ");
        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=888888888 saksnummer=SNR0002 ");
        sniffer.assertHasInfoMessage("foobar abac_action=null abac_resource_type=null aksjonspunktId=888888888 saksnummer=SNR0003 ");
        assertThat(sniffer.countEntries("action")).isEqualTo(6);
    }

    @Test
    public void skal_logge_fra_pdp_request() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        r.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        sporing.loggTilgang(r, attributter);
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X fnr=11111111111");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        r.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
            .leggTil(StandardAbacAttributtType.DOKUMENT_DATA_ID, 1000L)
            .leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR0001")
            .leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, "999999999")
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunktId=999999999 aksjonspunkt_type=X behandlingId=1234 dokumentDataId=1000 fnr=11111111111 saksnummer=SNR0001 ");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_en_rad_fra_pdp_request_og_flere_fra_attributer() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        r.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1235L)
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1236L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1234 fnr=11111111111");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1235 fnr=11111111111");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1236 fnr=11111111111");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_fler_rader_fra_pdp_request_og_en_fra_attributer() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();


        PdpRequest r = new PdpRequest();
        r.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222", "33333333333")));
        r.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1234 fnr=11111111111");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1234 fnr=22222222222");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1234 fnr=33333333333");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_ha_separate_rader_for_pdpRequest_og_attributter_når_det_er_flere_fra_hver_for_å_unngå_stort_kryssprodukt() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        r.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, new TreeSet<>(Arrays.asList("11111111111", "22222222222")));
        r.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett()
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L)
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1235L)
            .leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1236L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X fnr=11111111111 ");
        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X fnr=22222222222 ");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1234");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1235");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1236");
        assertThat(sniffer.countEntries("action")).isEqualTo(5);
    }

    @Test
    public void skal_erstatte_mellomrom_med_underscore_for_å_forenkle_parsing_av_sporingslogg() throws Exception {
        LegacyAbacSporingslogg sporing = new LegacyAbacSporingslogg();

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.SAKSNUMMER, "SNR 0001"));

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("saksnummer=SNR_0001");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter_ved_deny() throws Exception {
        var sporing = new LegacyAbacSporingslogg();

        var pdpRequest = new PdpRequest();
        pdpRequest.put(CommonAttributter.RESOURCE_FELLES_PERSON_FNR, Collections.singleton("11111111111"));
        pdpRequest.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token").setAction("foobar");
        attributter.leggTil(AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.BEHANDLING_ID, 1234L));

        sporing.loggDeny(pdpRequest, Collections.singletonList(Decision.Deny), attributter);

        sniffer.assertHasInfoMessage("action=foobar abac_action=null abac_resource_type=null aksjonspunkt_type=X behandlingId=1234 decision=Deny fnr=11111111111 ");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

}

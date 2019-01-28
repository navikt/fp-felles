package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import org.junit.Rule;
import org.junit.Test;

import no.nav.modig.core.test.LogSniffer;

public class AbacSporingsloggTest {

    @Rule
    public LogSniffer sniffer = new LogSniffer();

    @Test
    public void skal_logge_fra_attributter() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilBehandlingsId(1234L)
                .leggTilDokumentDataId(1000L)
                .leggTilSaksnummer("SNR0001")
                .leggTilAksjonspunktKode("999999999")
                .leggTilJournalPostId("JP001", true)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar saksnummer=SNR0001 behandlingId=1234 aksjonspunktId=999999999 journalpostId=JP001 dokumentDataId=1000 abac_action=null abac_resource_type=null");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_lage_flere_rader_når_en_attributt_har_flere_verdier() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilAksjonspunktKode("777777777")
                .leggTilAksjonspunktKode("888888888")
                .leggTilAksjonspunktKode("999999999")
                .leggTilBehandlingsId(1234L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar behandlingId=1234 aksjonspunktId=777777777 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1234 aksjonspunktId=888888888 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1234 aksjonspunktId=999999999 abac_action=null abac_resource_type=null");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_lage_kryssprodukt_når_det_er_noen_attributter_som_har_flere_verdier() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilSaksnummer("SNR0001")
                .leggTilSaksnummer("SNR0002")
                .leggTilSaksnummer("SNR0003")
                .leggTilAksjonspunktKode("888888888")
                .leggTilAksjonspunktKode("999999999")
                .leggTilJournalPostId("JP001", true)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0001 aksjonspunktId=999999999 journalpostId=JP001 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0002 aksjonspunktId=999999999 journalpostId=JP001 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0003 aksjonspunktId=999999999 journalpostId=JP001 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0001 aksjonspunktId=888888888 journalpostId=JP001 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0002 aksjonspunktId=888888888 journalpostId=JP001 abac_action=null abac_resource_type=null");
        sniffer.assertHasInfoMessage("foobar saksnummer=SNR0003 aksjonspunktId=888888888 journalpostId=JP001 abac_action=null abac_resource_type=null");
        assertThat(sniffer.countEntries("action")).isEqualTo(6);
    }

    @Test
    public void skal_logge_fra_pdp_request() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        r.setFnr(Collections.singleton("11111111111"));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        sporing.loggTilgang(r, attributter);
        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        r.setFnr(Collections.singleton("11111111111"));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilBehandlingsId(1234L)
                .leggTilDokumentDataId(1000L)
                .leggTilSaksnummer("SNR0001")
                .leggTilAksjonspunktKode("999999999")
                .leggTilJournalPostId("JP001", true)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar saksnummer=SNR0001 fnr=11111111111 behandlingId=1234 aksjonspunktId=999999999 journalpostId=JP001 dokumentDataId=1000 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_en_rad_fra_pdp_request_og_flere_fra_attributer() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        r.setFnr(Collections.singleton("11111111111"));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilBehandlingsId(1234L)
                .leggTilBehandlingsId(1235L)
                .leggTilBehandlingsId(1236L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 behandlingId=1234 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 behandlingId=1235 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 behandlingId=1236 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_sette_sammen_rader_når_det_kommer_fler_rader_fra_pdp_request_og_en_fra_attributer() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");


        PdpRequest r = new PdpRequest();
        r.setFnr(new TreeSet<>(Arrays.asList("11111111111", "22222222222", "33333333333")));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilBehandlingsId(1234L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 behandlingId=1234 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar fnr=22222222222 behandlingId=1234 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar fnr=33333333333 behandlingId=1234 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        assertThat(sniffer.countEntries("action")).isEqualTo(3);
    }

    @Test
    public void skal_ha_separate_rader_for_pdpRequest_og_attributter_når_det_er_flere_fra_hver_for_å_unngå_stort_kryssprodukt() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        r.setFnr(new TreeSet<>(Arrays.asList("11111111111", "22222222222")));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett()
                .leggTilBehandlingsId(1234L)
                .leggTilBehandlingsId(1235L)
                .leggTilBehandlingsId(1236L)
        );

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar fnr=22222222222 abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1234");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1235");
        sniffer.assertHasInfoMessage("action=foobar behandlingId=1236");
        assertThat(sniffer.countEntries("action")).isEqualTo(5);
    }

    @Test
    public void skal_erstatte_mellomrom_med_underscore_for_å_forenkle_parsing_av_sporingslogg() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett().leggTilSaksnummer("SNR 0001"));

        sporing.loggTilgang(r, attributter);

        sniffer.assertHasInfoMessage("saksnummer=SNR_0001");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

    @Test
    public void skal_logge_fra_pdp_request_og_attributter_ved_deny() throws Exception {
        AbacSporingslogg sporing = new AbacSporingslogg("foobar");

        PdpRequest r = new PdpRequest();
        r.setFnr(Collections.singleton("11111111111"));
        r.setAksjonspunktType(Collections.singleton("X"));
        AbacAttributtSamling attributter = AbacAttributtSamling.medJwtToken("dummy.oidc.token");
        attributter.leggTil(AbacDataAttributter.opprett().leggTilBehandlingsId(1234L));

        sporing.loggDeny(r, Collections.singletonList(Decision.Deny), attributter);

        sniffer.assertHasInfoMessage("action=foobar fnr=11111111111 behandlingId=1234 decision=Deny abac_action=null abac_resource_type=null aksjonspunkt_type=X");
        assertThat(sniffer.countEntries("action")).isEqualTo(1);
    }

}
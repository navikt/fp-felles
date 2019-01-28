package no.nav.vedtak.sikkerhet.abac;

import org.junit.Test;

import java.util.LinkedHashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class PdpRequestTest {

    @Test
    public void skal_lage_kryssprodukt_mellom_ident_og_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> fnr = new LinkedHashSet<>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        fnr.add("33333333333");
        fnr.add("44444444444");
        req.setFnr(fnr);
        LinkedHashSet<String> aktørId = new LinkedHashSet<>();
        aktørId.add("1111");
        aktørId.add("2222");
        req.setAktørId(aktørId);
        LinkedHashSet<String> at = new LinkedHashSet<>();
        at.add("a");
        at.add("b");
        req.setAksjonspunktType(at);

        assertThat(req.antallResources()).isEqualTo(12); //(4 fnr + 2 aktørId) * 2 at

        assertThat(req.getFnrForIndex(0).get()).isEqualTo("11111111111");
        assertThat(req.getFnrForIndex(1).get()).isEqualTo("22222222222");
        assertThat(req.getFnrForIndex(2).get()).isEqualTo("33333333333");
        assertThat(req.getFnrForIndex(3).get()).isEqualTo("44444444444");
        assertThat(req.getAktørIdForIndex(0).get()).isEqualTo("1111");
        assertThat(req.getAktørIdForIndex(1).get()).isEqualTo("2222");
        assertThat(req.getFnrForIndex(4).get()).isEqualTo("11111111111");
        assertThat(req.getFnrForIndex(5).get()).isEqualTo("22222222222");
        assertThat(req.getFnrForIndex(6).get()).isEqualTo("33333333333");
        assertThat(req.getFnrForIndex(7).get()).isEqualTo("44444444444");
        assertThat(req.getAktørIdForIndex(2).get()).isEqualTo("1111");
        assertThat(req.getAktørIdForIndex(3).get()).isEqualTo("2222");
        assertThat(req.getAksjonspunktTypeForIndex(0).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(1).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(2).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(3).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(4).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(5).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(6).get()).isEqualTo("b");
        assertThat(req.getAksjonspunktTypeForIndex(7).get()).isEqualTo("b");
        assertThat(req.getAksjonspunktTypeForIndex(8).get()).isEqualTo("b");
        assertThat(req.getAksjonspunktTypeForIndex(9).get()).isEqualTo("b");
        assertThat(req.getAksjonspunktTypeForIndex(10).get()).isEqualTo("b");
        assertThat(req.getAksjonspunktTypeForIndex(11).get()).isEqualTo("b");
    }

    @Test
    public void skal_fungere_uten_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> fnr = new LinkedHashSet<>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        req.setFnr(fnr);

        assertThat(req.antallResources()).isEqualTo(2);
        assertThat(req.getFnrForIndex(0).get()).isEqualTo("11111111111");
        assertThat(req.getFnrForIndex(1).get()).isEqualTo("22222222222");
        assertThat(req.getAksjonspunktTypeForIndex(0)).isNotPresent();
        assertThat(req.getAksjonspunktTypeForIndex(1)).isNotPresent();
    }

    @Test
    public void skal_fungere_uten_fnr() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> at = new LinkedHashSet<>();
        at.add("a");
        at.add("b");
        req.setAksjonspunktType(at);

        assertThat(req.antallResources()).isEqualTo(2);
        assertThat(req.getFnrForIndex(0)).isNotPresent();
        assertThat(req.getFnrForIndex(1)).isNotPresent();
        assertThat(req.getAksjonspunktTypeForIndex(0).get()).isEqualTo("a");
        assertThat(req.getAksjonspunktTypeForIndex(1).get()).isEqualTo("b");
    }

    @Test
    public void skal_fungere_uten_fnr_og_uten_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();

        assertThat(req.antallResources()).isEqualTo(1);
        assertThat(req.getFnrForIndex(0)).isNotPresent();
        assertThat(req.getAksjonspunktTypeForIndex(0)).isNotPresent();
    }
}

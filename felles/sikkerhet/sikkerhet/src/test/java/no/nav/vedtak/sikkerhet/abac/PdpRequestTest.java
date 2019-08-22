package no.nav.vedtak.sikkerhet.abac;

import static no.nav.abac.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.xacml.ForeldrepengerAttributter.RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class PdpRequestTest {

    @Test
    public void skal_lage_kryssprodukt_mellom_ident_og_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> fnr = new LinkedHashSet<>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        fnr.add("33333333333");
        fnr.add("44444444444");
        req.put(RESOURCE_FELLES_PERSON_FNR, fnr);
        LinkedHashSet<String> aktørId = new LinkedHashSet<>();
        aktørId.add("1111");
        aktørId.add("2222");
        req.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);
        LinkedHashSet<String> at = new LinkedHashSet<>();
        at.add("a");
        at.add("b");
        req.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, at);

        assertThat(antallResources(req)).isEqualTo(12); //(4 fnr + 2 aktørId) * 2 at

        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("11111111111"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("22222222222"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 2)).hasValueSatisfying(it -> assertThat(it).isEqualTo("33333333333"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 3)).hasValueSatisfying(it -> assertThat(it).isEqualTo("44444444444"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("1111"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("2222"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 4)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 5)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 6)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 7)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 2)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 3)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("a"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("b"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 2)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 3)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 4)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 5)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 6)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 7)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 8)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 9)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 10)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 11)).isNotPresent();
    }

    @Test
    public void skal_fungere_uten_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> fnr = new LinkedHashSet<>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        req.put(RESOURCE_FELLES_PERSON_FNR, fnr);

        assertThat(antallResources(req)).isEqualTo(2);
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("11111111111"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("22222222222"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 1)).isNotPresent();
    }

    @Test
    public void skal_fungere_uten_fnr() throws Exception {
        PdpRequest req = new PdpRequest();
        LinkedHashSet<String> at = new LinkedHashSet<>();
        at.add("a");
        at.add("b");
        req.put(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, at);

        assertThat(antallResources(req)).isEqualTo(2);
        assertThat(req.getListOfString(RESOURCE_FELLES_PERSON_FNR)).isEmpty();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 1)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("a"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("b"));
    }

    @Test
    public void skal_fungere_uten_fnr_og_uten_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();

        assertThat(antallResources(req)).isEqualTo(1);
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE, 0)).isNotPresent();
    }

    private Optional<String> getElementFromListByKeyAndIndex(PdpRequest pdpRequest, String key, int index) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index));
        }
        return Optional.empty();
    }

    private int antallResources(PdpRequest pdpRequest) {
        return Math.max(1, antallIdenter(pdpRequest)) * Math.max(1, antallAksjonspunktTyper(pdpRequest));
    }

    private int antallIdenter(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE) + pdpRequest.getAntall(RESOURCE_FELLES_PERSON_FNR);
    }

    private int antallAksjonspunktTyper(PdpRequest pdpRequest) {
        return pdpRequest.getAntall(RESOURCE_FORELDREPENGER_SAK_AKSJONSPUNKT_TYPE);
    }
}

package no.nav.vedtak.sikkerhet.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_NAVN;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class PdpRequestTest {

    @Test
    public void skal_lage_kryssprodukt_mellom_identer() throws Exception {
        PdpRequest req = new PdpRequest();
        var fnr = new LinkedHashSet<>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        fnr.add("33333333333");
        fnr.add("44444444444");
        req.put(RESOURCE_FELLES_PERSON_FNR, fnr);
        var aktørId = new LinkedHashSet<>();
        aktørId.add("1111");
        aktørId.add("2222");
        req.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørId);

        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("11111111111"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("22222222222"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 2)).hasValueSatisfying(it -> assertThat(it).isEqualTo("33333333333"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 3)).hasValueSatisfying(it -> assertThat(it).isEqualTo("44444444444"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 0))
            .hasValueSatisfying(it -> assertThat(it).isEqualTo("1111"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 1))
            .hasValueSatisfying(it -> assertThat(it).isEqualTo("2222"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 4)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 5)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 6)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 7)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 2)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, 3)).isNotPresent();
    }

    @Test
    public void skal_fungere_uten_fnr() throws Exception {
        PdpRequest req = new PdpRequest();
        var at = new LinkedHashSet<>();
        at.add("a");
        at.add("b");
        req.put(RESOURCE_FELLES_PERSON_NAVN, at);

        assertThat(req.getListOfString(RESOURCE_FELLES_PERSON_FNR)).isEmpty();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 1)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_NAVN, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("a"));
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_NAVN, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("b"));
    }

    @Test
    public void skal_fungere_uten_fnr_og_uten_aksjonspunkt_type() throws Exception {
        PdpRequest req = new PdpRequest();

        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_FNR, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, RESOURCE_FELLES_PERSON_NAVN, 0)).isNotPresent();
    }

    private Optional<String> getElementFromListByKeyAndIndex(PdpRequest pdpRequest, String key, int index) {
        List<String> list = pdpRequest.getListOfString(key);
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index));
        }
        return Optional.empty();
    }

}

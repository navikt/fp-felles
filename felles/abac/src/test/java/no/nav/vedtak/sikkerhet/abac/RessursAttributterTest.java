package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursData;

class RessursAttributterTest {

    @Test
    void skal_lage_kryssprodukt_mellom_identer() {
        var identer = new LinkedHashSet<String>();
        identer.add("11111111111");
        identer.add("22222222222");
        identer.add("33333333333");
        identer.add("44444444444");
        identer.add("1111");
        identer.add("2222");
        var req = AppRessursData.builder().leggTilIdenter(identer).build();

        assertThat(getIdentFromList(req, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("11111111111"));
        assertThat(getIdentFromList(req, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("22222222222"));
        assertThat(getIdentFromList(req, 2)).hasValueSatisfying(it -> assertThat(it).isEqualTo("33333333333"));
        assertThat(getIdentFromList(req, 3)).hasValueSatisfying(it -> assertThat(it).isEqualTo("44444444444"));
        assertThat(getIdentFromList(req, 4)).hasValueSatisfying(it -> assertThat(it).isEqualTo("1111"));
        assertThat(getIdentFromList(req, 5)).hasValueSatisfying(it -> assertThat(it).isEqualTo("2222"));
        assertThat(getIdentFromList(req, 6)).isNotPresent();
    }

    @Test
    void skal_fungere_uten_fnr() {
        var req = AppRessursData.builder().medAnsvarligSaksbehandler("A000000").build();

        assertThat(getIdentFromList(req, 0)).isNotPresent();
        assertThat(getIdentFromList(req, 1)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 0)).hasValueSatisfying(
            it -> assertThat(it).isEqualTo("A000000"));
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 1)).isNotPresent();
    }

    @Test
    void skal_fungere_uten_fnr_og_uten_aksjonspunkt_type() {
        var req = AppRessursData.builder().build();

        assertThat(getIdentFromList(req, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 0)).isNotPresent();
    }

    private static Optional<String> getElementFromListByKeyAndIndex(AppRessursData appRessursData, ForeldrepengerDataKeys key, int index) {
        var list = Optional.ofNullable(appRessursData.getResource(key)).map(List::of).orElse(List.of());
        if (list.size() >= index + 1) {
            return Optional.of(list.get(index)).map(RessursData::verdi);
        }
        return Optional.empty();
    }

    private static Optional<String> getIdentFromList(AppRessursData appRessursData, int index) {
        List<String> list = appRessursData.getIdenter().stream().toList();
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index));
        }
        return Optional.empty();
    }


}

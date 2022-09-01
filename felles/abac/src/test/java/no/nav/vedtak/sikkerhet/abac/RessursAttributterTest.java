package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.RessursDataKey;

class RessursAttributterTest {

    @Test
    void skal_lage_kryssprodukt_mellom_identer() throws Exception {
        var fnr = new LinkedHashSet<String>();
        fnr.add("11111111111");
        fnr.add("22222222222");
        fnr.add("33333333333");
        fnr.add("44444444444");
        var aktørId = new LinkedHashSet<String>();
        aktørId.add("1111");
        aktørId.add("2222");
        var req = AppRessursData.builder()
            .leggTilFødselsnumre(fnr).leggTilAktørIdSet(aktørId)
            .build();

        assertThat(getFnrFromList(req, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("11111111111"));
        assertThat(getFnrFromList(req, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("22222222222"));
        assertThat(getFnrFromList(req, 2)).hasValueSatisfying(it -> assertThat(it).isEqualTo("33333333333"));
        assertThat(getFnrFromList(req, 3)).hasValueSatisfying(it -> assertThat(it).isEqualTo("44444444444"));
        assertThat(getAktørIdFromList(req, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("1111"));
        assertThat(getAktørIdFromList(req, 1)).hasValueSatisfying(it -> assertThat(it).isEqualTo("2222"));
        assertThat(getFnrFromList(req, 4)).isNotPresent();
        assertThat(getFnrFromList(req, 5)).isNotPresent();
        assertThat(getFnrFromList(req, 6)).isNotPresent();
        assertThat(getFnrFromList(req, 7)).isNotPresent();
        assertThat(getAktørIdFromList(req, 2)).isNotPresent();
        assertThat(getAktørIdFromList(req, 3)).isNotPresent();
    }

    @Test
    void skal_fungere_uten_fnr() throws Exception {
        var req = AppRessursData.builder()
            .leggTilRessurs(ForeldrepengerDataKeys.SAKSBEHANDLER, "A000000")
            .build();

        assertThat(getFnrFromList(req, 0)).isNotPresent();
        assertThat(getFnrFromList(req, 1)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 0)).hasValueSatisfying(it -> assertThat(it).isEqualTo("A000000"));
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 1)).isNotPresent();
    }

    @Test
    void skal_fungere_uten_fnr_og_uten_aksjonspunkt_type() throws Exception {
        var req = AppRessursData.builder().build();

        assertThat(getFnrFromList(req, 0)).isNotPresent();
        assertThat(getElementFromListByKeyAndIndex(req, ForeldrepengerDataKeys.SAKSBEHANDLER, 0)).isNotPresent();
    }

    private static Optional<String> getElementFromListByKeyAndIndex(AppRessursData appRessursData, RessursDataKey key, int index) {
        var list = Optional.ofNullable(appRessursData.getResource(key)).map(List::of).orElse(List.of());
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index)).map(RessursData::verdi);
        }
        return Optional.empty();
    }

    private static Optional<String> getFnrFromList(AppRessursData appRessursData, int index) {
        List<String> list = appRessursData.getFødselsnumre().stream().toList();
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index));
        }
        return Optional.empty();
    }

    private static Optional<String> getAktørIdFromList(AppRessursData appRessursData, int index) {
        List<String> list = appRessursData.getAktørIdSet().stream().toList();
        if (list.size() >= index + 1) {
            return Optional.ofNullable(list.get(index));
        }
        return Optional.empty();
    }

}

package no.nav.vedtak.felles.integrasjon.oppgave.v1;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PatchReservasjonTest {

    @Test
    void reservasjonSerdes() {
        var testBruker = "testBruker";
        var objekt = new PatchReservasjon(1L, 1, testBruker);

        var ser = DefaultJsonMapper.toJson(objekt);

        assertThat(ser).contains(String.format("{\"id\":1,\"versjon\":1,\"tilordnetRessurs\":\"%s\"}",testBruker));

        var des = DefaultJsonMapper.fromJson(ser, PatchReservasjon.class);

        assertThat(des.tilordnetRessurs()).isEqualTo(testBruker);
    }

    @Test
    void avreservasjonSerdes() {
        var patchObjekt = new PatchReservasjon(1L, 1, null);

        var ser = DefaultJsonMapper.toJson(patchObjekt);

        assertThat(ser).contains("{\"id\":1,\"versjon\":1,\"tilordnetRessurs\":null}");

        var des = DefaultJsonMapper.fromJson(ser, PatchReservasjon.class);

        assertThat(des.tilordnetRessurs()).isNull();
    }
}

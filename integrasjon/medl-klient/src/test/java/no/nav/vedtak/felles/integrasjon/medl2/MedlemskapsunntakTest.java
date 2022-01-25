package no.nav.vedtak.felles.integrasjon.medl2;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class MedlemskapsunntakTest {

    private static final long MEDL_ID_1 = 2663947L;
    private static final long MEDL_ID_2 = 2663948L;
    private static final long MEDL_ID_3 = 666L;

    @Test
    void roundtrip_rest_1() {
        var mrest = new Medlemskapsunntak(MEDL_ID_1,
                LocalDate.of(2019, 8, 1),
                LocalDate.of(2019, 12, 31),
                "Full",
                "MEDFT",
                "ENDL",
                "UZB",
                null,
                true,
                new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2020, 5, 26), "AVGSYS"),
                new Medlemskapsunntak.Studieinformasjon("VUT"));
        var json = DefaultJsonMapper.toJson(mrest);
        var dser = DefaultJsonMapper.fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getLovvalg()).isEqualTo(dser.getLovvalg());
        assertThat(mrest.isMedlem()).isEqualTo(dser.isMedlem());
    }

    @Test
    void roundtrip_rest_2() {
        var mrest = new Medlemskapsunntak(MEDL_ID_2,
                LocalDate.of(2019, 8, 1),
                LocalDate.of(2019, 12, 31),
                "FTL_2_9_1_a",
                "MEDFT",
                "ENDL",
                null,
                null,
                true,
                new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2020, 5, 26), "AVGSYS"),
                null);
        var json = DefaultJsonMapper.toJson(mrest);
        var dser = DefaultJsonMapper.fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getDekning()).isEqualTo(dser.getDekning());
        assertThat(mrest.getKilde()).isEqualTo(dser.getKilde());
        assertThat(mrest.getBesluttet()).isEqualTo(dser.getBesluttet());
        assertThat(mrest.getStudieland()).isEqualTo(dser.getStudieland());
    }

    @Test
    void roundtrip_rest_3() {
        var mrest = new Medlemskapsunntak(MEDL_ID_3,
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2019, 2, 28),
                "Full",
                "MEDFT",
                "UAVK",
                null,
                null,
                true,
                new Medlemskapsunntak.Sporingsinformasjon(LocalDate.of(2019, 5, 26), "LAANEKASSEN"),
                new Medlemskapsunntak.Studieinformasjon("SWE"));
        var json = DefaultJsonMapper.toJson(mrest);
        var dser = DefaultJsonMapper.fromJson(json, Medlemskapsunntak.class);
        assertThat(mrest).isEqualTo(dser);
        assertThat(mrest.getFraOgMed()).isEqualTo(dser.getFraOgMed());
        assertThat(mrest.getStudieland()).isEqualTo(dser.getStudieland());
    }
}

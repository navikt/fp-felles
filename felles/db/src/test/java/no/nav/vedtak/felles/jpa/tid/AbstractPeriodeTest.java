package no.nav.vedtak.felles.jpa.tid;

import org.junit.Test;
import org.threeten.extra.Interval;

import no.nav.vedtak.felles.jpa.tid.AbstractLocalDateInterval;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractPeriodeTest {

    private static final LocalDate NÅ = LocalDate.now();

    private static class TestAvPeriode extends AbstractLocalDateInterval {

        private LocalDate fomDato;
        private LocalDate tomDato;

        TestAvPeriode(LocalDate fomDato, LocalDate tomDato) {
            this.fomDato = fomDato;
            this.tomDato = tomDato;
        }

        @Override
        public LocalDate getFomDato() {
            return fomDato;
        }

        @Override
        public LocalDate getTomDato() {
            return tomDato;
        }

        @Override
        protected AbstractLocalDateInterval lagNyPeriode(LocalDate fomDato, LocalDate tomDato) {
            return new TestAvPeriode(fomDato, tomDato);
        }

    }

    @Test
    public void skal_konvertere_periode_til_intervall() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));

        Interval intervall = periode.tilIntervall();

        assertThat(intervall.getStart()).isEqualTo(NÅ.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        assertThat(intervall.getEnd()).isEqualTo(NÅ.plusDays(10).plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void skal_sjekke_at_dato_er_før_periodens_slutt() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erFørEllerLikPeriodeslutt = periode.erFørEllerLikPeriodeslutt(NÅ.plusDays(5));
        assertThat(erFørEllerLikPeriodeslutt).isTrue();
    }

    @Test
    public void skal_sjekke_at_dato_er_lik_periodens_slutt() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erFørEllerLikPeriodeslutt = periode.erFørEllerLikPeriodeslutt(NÅ.plusDays(10));
        assertThat(erFørEllerLikPeriodeslutt).isTrue();
    }

    @Test
    public void skal_sjekke_at_dato_er_etter_periodens_slutt() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erFørEllerLikPeriodeslutt = periode.erFørEllerLikPeriodeslutt(NÅ.plusDays(11));
        assertThat(erFørEllerLikPeriodeslutt).isFalse();
    }

    @Test
    public void skal_sjekke_at_dato_er_etter_periodens_start() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erEtterEllerLikPeriodestart = periode.erEtterEllerLikPeriodestart(NÅ.plusDays(5));
        assertThat(erEtterEllerLikPeriodestart).isTrue();
    }

    @Test
    public void skal_sjekke_at_dato_er_lik_periodens_start() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erEtterEllerLikPeriodestart = periode.erEtterEllerLikPeriodestart(NÅ);
        assertThat(erEtterEllerLikPeriodestart).isTrue();
    }

    @Test
    public void skal_sjekke_at_dato_er_før_periodens_start() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        boolean erEtterEllerLikPeriodestart = periode.erEtterEllerLikPeriodestart(NÅ.minusDays(1));
        assertThat(erEtterEllerLikPeriodestart).isFalse();
    }

    @Test
    public void skal_sjekke_at_dato_finnes_i_periode() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        assertThat(periode.inkluderer(NÅ)).isTrue();
        assertThat(periode.inkluderer(NÅ.plusDays(10))).isTrue();
    }

    @Test
    public void skal_sjekke_at_dato_ikke_finnes_i_periode() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        assertThat(periode.inkluderer(NÅ.minusDays(1))).isFalse();
        assertThat(periode.inkluderer(NÅ.plusDays(11))).isFalse();
    }

    @Test
    public void skal_finne_antall_dager_i_periode() {
        AbstractLocalDateInterval periodeVinter = new TestAvPeriode(LocalDate.of(2016, Month.DECEMBER, 1), LocalDate.of(2016, Month.DECEMBER, 11));
        assertThat(periodeVinter.antallDager()).isEqualTo(10);

        AbstractLocalDateInterval periodeSommer = new TestAvPeriode(LocalDate.of(2016, Month.JUNE, 1), LocalDate.of(2016, Month.JUNE, 11));
        assertThat(periodeSommer.antallDager()).isEqualTo(10);
    }

    @Test
    public void skal_sjekke_at_periode_overlapper_annen_periode() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        AbstractLocalDateInterval periode2 = new TestAvPeriode(NÅ.minusDays(5), NÅ);
        AbstractLocalDateInterval periode3 = new TestAvPeriode(NÅ.minusDays(5), NÅ.plusDays(5));
        AbstractLocalDateInterval periode4 = new TestAvPeriode(NÅ.minusDays(9), NÅ.plusDays(15));
        AbstractLocalDateInterval periode5 = new TestAvPeriode(NÅ.minusDays(10), NÅ.plusDays(15));

        assertThat(periode.overlapper(periode2)).isTrue();
        assertThat(periode.overlapper(periode3)).isTrue();
        assertThat(periode.overlapper(periode4)).isTrue();
        assertThat(periode.overlapper(periode5)).isTrue();
    }

    @Test
    public void skal_sjekke_at_periode_ikke_overlapper_annen_periode() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        AbstractLocalDateInterval periode2 = new TestAvPeriode(NÅ.minusDays(5), NÅ.minusDays(1));
        AbstractLocalDateInterval periode3 = new TestAvPeriode(NÅ.plusDays(11), NÅ.plusDays(15));

        assertThat(periode.overlapper(periode2)).isFalse();
        assertThat(periode.overlapper(periode3)).isFalse();
    }

    @Test
    public void arbeidsdager_over_to_helger_skal_bli_7() {
        AbstractLocalDateInterval fredagTilOgMedMandagOverToHelger = new TestAvPeriode(LocalDate.of(2016, Month.NOVEMBER, 25),
                LocalDate.of(2016, Month.DECEMBER, 5));

        assertThat(fredagTilOgMedMandagOverToHelger.antallArbeidsdager()).isEqualTo(7);
    }

    @Test
    public void antall_arbeidsdager_med_samme_fom_og_tom_skal_bli_1() {
        AbstractLocalDateInterval fredagTilOgMedMandagOverToHelger = new TestAvPeriode(LocalDate.of(2016, Month.NOVEMBER, 25),
                LocalDate.of(2016, Month.NOVEMBER, 25));

        assertThat(fredagTilOgMedMandagOverToHelger.antallArbeidsdager()).isEqualTo(1);
    }

    @Test
    public void skal_ha_perioder_som_grenser_til_hverandre() {
        AbstractLocalDateInterval periode1 = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        AbstractLocalDateInterval periode2 = new TestAvPeriode(NÅ.plusDays(11), NÅ.plusDays(15));
        AbstractLocalDateInterval periode3 = new TestAvPeriode(NÅ.minusDays(10), NÅ.minusDays(1));
        assertThat(periode1.grenserTil(periode2)).isTrue();
        assertThat(periode1.grenserTil(periode3)).isTrue();
    }

    @Test
    public void skal_ha_perioder_som_ikke_grenser_til_hverandre() {
        AbstractLocalDateInterval periode1 = new TestAvPeriode(NÅ, NÅ.plusDays(10));
        AbstractLocalDateInterval periode2 = new TestAvPeriode(NÅ.plusDays(10), NÅ.plusDays(15));
        AbstractLocalDateInterval periode3 = new TestAvPeriode(NÅ.minusDays(10), NÅ);
        AbstractLocalDateInterval periode4 = new TestAvPeriode(NÅ.minusDays(10), NÅ.minusDays(2));
        assertThat(periode1.grenserTil(periode2)).isFalse();
        assertThat(periode1.grenserTil(periode3)).isFalse();
        assertThat(periode1.grenserTil(periode4)).isFalse();
    }

    @Test
    public void skal_finne_arbeidsdagene_i_en_periode() {
        LocalDate mandag = LocalDate.of(2016, Month.NOVEMBER, 21);
        LocalDate søndag = LocalDate.of(2016, Month.NOVEMBER, 27);
        AbstractLocalDateInterval periode1 = new TestAvPeriode(mandag, søndag);

        assertThat(periode1.arbeidsdager()).containsOnly(
                mandag,
                mandag.plusDays(1),
                mandag.plusDays(2),
                mandag.plusDays(3),
                mandag.plusDays(4));
    }

    @Test
    public void skal_splitte_periode_på_månedsgrenser_når_periode_starter_den_første_og_slutter_på_månedens_siste_dag() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 1);
        LocalDate slutt = LocalDate.of(2016, Month.OCTOBER, 31);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        List<AbstractLocalDateInterval> månedsperioder = periode.splittVedMånedsgrenser();

        assertThat(månedsperioder).containsOnly(
                new TestAvPeriode(start, LocalDate.of(2016, Month.AUGUST, 31)),
                new TestAvPeriode(LocalDate.of(2016, Month.SEPTEMBER, 1), LocalDate.of(2016, Month.SEPTEMBER, 30)),
                new TestAvPeriode(LocalDate.of(2016, Month.OCTOBER, 1), slutt));

    }

    @Test
    public void skal_splitte_periode_på_månedsgrenser_når_periode_starter_i_midten_av_måned() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate slutt = LocalDate.of(2016, Month.OCTOBER, 31);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        List<AbstractLocalDateInterval> månedsperioder = periode.splittVedMånedsgrenser();

        assertThat(månedsperioder).containsOnly(
                new TestAvPeriode(start, LocalDate.of(2016, Month.AUGUST, 31)),
                new TestAvPeriode(LocalDate.of(2016, Month.SEPTEMBER, 1), LocalDate.of(2016, Month.SEPTEMBER, 30)),
                new TestAvPeriode(LocalDate.of(2016, Month.OCTOBER, 1), slutt));
    }

    @Test
    public void skal_splitte_periode_på_månedsgrenser_når_periode_slutter_i_midten_av_måned() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate slutt = LocalDate.of(2016, Month.OCTOBER, 15);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        List<AbstractLocalDateInterval> månedsperioder = periode.splittVedMånedsgrenser();

        assertThat(månedsperioder).containsOnly(
                new TestAvPeriode(start, LocalDate.of(2016, Month.AUGUST, 31)),
                new TestAvPeriode(LocalDate.of(2016, Month.SEPTEMBER, 1), LocalDate.of(2016, Month.SEPTEMBER, 30)),
                new TestAvPeriode(LocalDate.of(2016, Month.OCTOBER, 1), slutt));
    }

    @Test
    public void skal_ikke_splitte_i_måneder_når_periode_er_innenfor_en_måned() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate slutt = LocalDate.of(2016, Month.AUGUST, 25);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        List<AbstractLocalDateInterval> månedsperioder = periode.splittVedMånedsgrenser();

        assertThat(månedsperioder).containsOnly(new TestAvPeriode(start, slutt));
    }

    @Test
    public void skal_finne_maks_antall_arbeidsdager_i_periode() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate slutt = LocalDate.of(2016, Month.AUGUST, 25);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        long maksAntall = periode.maksAntallArbeidsdager();

        assertThat(maksAntall).isEqualTo(23);
    }

    @Test
    public void skal_finne_maks_antall_arbeidsdager_i_periode_på_to_måneder() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 15);
        LocalDate slutt = LocalDate.of(2016, Month.SEPTEMBER, 25);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        long maksAntall = periode.maksAntallArbeidsdager();

        assertThat(maksAntall).isEqualTo(45);
    }

    @Test
    public void skal_finne_månedskvantum_for_å_rekne_ut_lønn_i_periode_på_akkurat_to_måneder() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 01);
        LocalDate slutt = LocalDate.of(2016, Month.SEPTEMBER, 30);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        double kvantum = periode.finnMånedeskvantum();

        assertThat(kvantum).isEqualTo(2);
        assertThat(kvantum * 15000).isEqualTo(30000);
    }

    @Test
    public void skal_finne_månedskvantum_for_å_rekne_ut_lønn_i_periode_på_to_og_en_halv_måned() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 01);
        LocalDate slutt = LocalDate.of(2016, Month.SEPTEMBER, 15);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);

        double kvantum = periode.finnMånedeskvantum();

        assertThat(kvantum).isEqualTo(1.5);
        assertThat(kvantum * 15000).isEqualTo(22500);
    }

    @Test
    public void skal_kutte_periode_på_grensene_til_ny_periode() {
        LocalDate start = LocalDate.of(2016, Month.AUGUST, 01);
        LocalDate slutt = LocalDate.of(2016, Month.DECEMBER, 15);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);
        LocalDate start1 = LocalDate.of(2016, Month.SEPTEMBER, 01);
        LocalDate slutt1 = LocalDate.of(2016, Month.OCTOBER, 15);
        AbstractLocalDateInterval periode1 = new TestAvPeriode(start1, slutt1);

        AbstractLocalDateInterval nyPeriode = periode.kuttPeriodePåGrenseneTil(periode1);

        assertThat(nyPeriode).isEqualTo(periode1);
    }

    @Test
    public void skal_kutte() {
        LocalDate start = LocalDate.of(2015, Month.JULY, 1);
        LocalDate slutt = LocalDate.of(2018, Month.JULY, 31);
        AbstractLocalDateInterval periode = new TestAvPeriode(start, slutt);
        LocalDate start1 = LocalDate.of(2018, Month.JANUARY, 1);
        LocalDate slutt1 = LocalDate.of(2019, Month.JANUARY, 1);
        AbstractLocalDateInterval periode1 = new TestAvPeriode(start1, slutt1);

        AbstractLocalDateInterval nyPeriode = periode.kuttPeriodePåGrenseneTil(periode1);

        assertThat(nyPeriode).isEqualTo(new TestAvPeriode(
                LocalDate.of(2018, Month.JANUARY, 1),
                LocalDate.of(2018, Month.JULY, 31)));
    }

    @Test
    public void skal_kutte_periode_på_gitte_datoer() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusMonths(3));

        List<AbstractLocalDateInterval> perioder = periode.splittPeriodePåDatoer(NÅ.plusDays(10), NÅ.plusMonths(1));

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0)).isEqualTo(new TestAvPeriode(NÅ, NÅ.plusDays(9)));
        assertThat(perioder.get(1)).isEqualTo(new TestAvPeriode(NÅ.plusDays(10), NÅ.plusMonths(1).minusDays(1)));
        assertThat(perioder.get(2)).isEqualTo(new TestAvPeriode(NÅ.plusMonths(1), NÅ.plusMonths(3)));
    }

    @Test
    public void skal_kutte_periode_på_gitte_datoer_og_avgrense_til_hverdager() {
        AbstractLocalDateInterval periode = new TestAvPeriode(NÅ, NÅ.plusMonths(3));

        List<AbstractLocalDateInterval> perioder = periode.splittPeriodePåDatoerAvgrensTilArbeidsdager(NÅ.plusDays(10), NÅ.plusMonths(1));

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0)).isEqualTo(new TestAvPeriode(NÅ, NÅ.plusDays(9)).avgrensTilArbeidsdager());
        assertThat(perioder.get(1)).isEqualTo(new TestAvPeriode(NÅ.plusDays(10), NÅ.plusMonths(1).minusDays(1)).avgrensTilArbeidsdager());
        assertThat(perioder.get(2)).isEqualTo(new TestAvPeriode(NÅ.plusMonths(1), NÅ.plusMonths(3)).avgrensTilArbeidsdager());
    }

    @Test
    public void skal_kutte_periode_på_gitte_datoer_og_avgrense_til_hverdager_når_input_dato_er_en_helgedag() {
        AbstractLocalDateInterval periode = new TestAvPeriode(LocalDate.of(2018, Month.DECEMBER, 31), LocalDate.of(2019, Month.SEPTEMBER, 28));

        List<AbstractLocalDateInterval> perioder = periode.splittPeriodePåDatoerAvgrensTilArbeidsdager(
                LocalDate.of(2019, Month.JANUARY, 21), LocalDate.of(2019, Month.MARCH, 30));

        assertThat(perioder).hasSize(3);
        assertThat(perioder.get(0))
                .isEqualTo(new TestAvPeriode(LocalDate.of(2018, Month.DECEMBER, 31), LocalDate.of(2019, Month.JANUARY, 18)));
        assertThat(perioder.get(1))
                .isEqualTo(new TestAvPeriode(LocalDate.of(2019, Month.JANUARY, 21), LocalDate.of(2019, Month.MARCH, 29)));
        assertThat(perioder.get(2))
                .isEqualTo(new TestAvPeriode(LocalDate.of(2019, Month.APRIL, 1), LocalDate.of(2019, Month.SEPTEMBER, 27)));
    }
}

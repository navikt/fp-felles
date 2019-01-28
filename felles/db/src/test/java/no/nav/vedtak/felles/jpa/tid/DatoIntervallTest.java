package no.nav.vedtak.felles.jpa.tid;

import org.junit.Test;

import no.nav.vedtak.felles.jpa.tid.AbstractLocalDateInterval;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class DatoIntervallTest {

    private static final LocalDate NÅ = LocalDate.now();

    @Test
    public void test_metode_finnFomDato() {
        LocalDate dateFrom = LocalDate.of(2018,03,01);
        LocalDate dateToHave = LocalDate.of(2018, 02, 21);
        LocalDate date = AbstractLocalDateInterval.finnFomDato(dateFrom, 7);
        assertThat(date.compareTo(dateToHave)).isEqualTo(0);

    }

    @Test
    public void skal_opprette_periode_som_varer_fra_nå_til_tidenes_ende() {
        AbstractLocalDateInterval periode = DatoIntervallEntitet.fraOgMed(NÅ);
        assertThat(periode.getFomDato()).isEqualTo(NÅ);
        assertThat(periode.getTomDato()).isEqualTo(AbstractLocalDateInterval.TIDENES_ENDE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void det_skal_ikke_være_mulig_å_opprette_vrengte_perioder() {
        DatoIntervallEntitet.fraOgMedTilOgMed(LocalDate.now(), LocalDate.now().minusDays(1));
    }

    @Test(expected = IllegalStateException.class)
    public void antall_arbeidsdager_med_uten_tom_skal_gi_max_illegal_argument_exception() {
        AbstractLocalDateInterval fredagTilOgMedMandagOverToHelger = DatoIntervallEntitet.fraOgMed(LocalDate.of(2016, Month.NOVEMBER,
                25));

        fredagTilOgMedMandagOverToHelger.antallArbeidsdager();
    }

    @Test
    public void lage_periode_med_1_arbeidsdag() {
        AbstractLocalDateInterval periode = DatoIntervallEntitet.fraOgMedPlusArbeidsdager(LocalDate.of(2016, Month.NOVEMBER, 25), 1);

        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2016, Month.NOVEMBER, 25)); // fredag
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2016, Month.NOVEMBER, 25)); // mandag
    }

    @Test
    public void lage_periode_med_2_arbeidsdager_over_en_helg() {
        AbstractLocalDateInterval periode = DatoIntervallEntitet.fraOgMedPlusArbeidsdager(LocalDate.of(2016, Month.NOVEMBER, 25), 2);

        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2016, Month.NOVEMBER, 25)); // fredag
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2016, Month.NOVEMBER, 28)); // mandag
    }

    @Test(expected = IllegalArgumentException.class)
    public void lage_periode_med_0_arbeidsdager_skal_gi_exception() {
        DatoIntervallEntitet.fraOgMedPlusArbeidsdager(LocalDate.of(2016, Month.NOVEMBER, 25), 0);
    }

    @Test
    public void lage_periode_1_arbeidsdag_tilbake() {
        AbstractLocalDateInterval periode = DatoIntervallEntitet.tilOgMedMinusArbeidsdager(LocalDate.of(2018, Month.MARCH, 12), 1);

        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2018, Month.MARCH, 12));
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2018, Month.MARCH, 12));
    }

    @Test
    public void lage_periode_2_arbeidsdager_tilbake_over_en_helg() {
        AbstractLocalDateInterval periode = DatoIntervallEntitet.tilOgMedMinusArbeidsdager(LocalDate.of(2018, Month.MARCH, 12), 2);

        assertThat(periode.getFomDato()).isEqualTo(LocalDate.of(2018, Month.MARCH, 9)); // fredag
        assertThat(periode.getTomDato()).isEqualTo(LocalDate.of(2018, Month.MARCH, 12)); // mandag
    }

    @Test(expected = IllegalArgumentException.class)
    public void lage_periode_med_0_arbeidsdager_tilbake_skal_gi_exception() {
        DatoIntervallEntitet.tilOgMedMinusArbeidsdager(LocalDate.of(2018, Month.MARCH, 12), 0);
    }

    @Test
    public void test_lag_ny_periode() {
        LocalDate fomDato = LocalDate.of(2018, Month.MARCH, 12); // mandag
        LocalDate tomDato = LocalDate.of(2018, Month.MARCH, 14); // onsdag

        DatoIntervallEntitet periode = DatoIntervallEntitet.fraOgMedPlusArbeidsdager(LocalDate.of(2016, Month.NOVEMBER, 25), 2);
        DatoIntervallEntitet datoIntervallEntitet = periode.lagNyPeriode(fomDato, tomDato);

        assertThat(datoIntervallEntitet.getFomDato()).isEqualTo(fomDato);
        assertThat(datoIntervallEntitet.getTomDato()).isEqualTo(tomDato);

    }
}

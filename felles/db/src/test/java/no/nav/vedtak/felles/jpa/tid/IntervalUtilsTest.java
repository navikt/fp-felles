package no.nav.vedtak.felles.jpa.tid;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Test;

public class IntervalUtilsTest {

    @Test
    public void skal_sjekke_om_interval_overlapper_et_annet() throws Exception {
        LocalDate iDag = LocalDate.now();
        LocalDate iMorra = LocalDate.now().plusDays(1);
        LocalDate om2Dager = LocalDate.now().plusDays(2);
        LocalDate om3Dager = LocalDate.now().plusDays(3);

        IntervalUtils intervall1 = new IntervalUtils(iDag, iMorra);
        IntervalUtils intervall2 = new IntervalUtils(iMorra, om2Dager);
        IntervalUtils intervall3 = new IntervalUtils(om2Dager, om3Dager);

        assertThat(intervall1.overlapper(intervall2)).isTrue();
        assertThat(intervall1.overlapper(intervall3)).isFalse();
    }
}
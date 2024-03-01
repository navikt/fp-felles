package no.nav.vedtak.konfig;

import java.time.LocalDate;
import java.time.Month;

public class Tid {

    public static final LocalDate TIDENES_BEGYNNELSE = LocalDate.of(-4712, Month.JANUARY, 1);
    public static final LocalDate TIDENES_ENDE = LocalDate.of(9999, Month.DECEMBER, 31);

    private Tid() {
        // hidden
    }

    public static LocalDate fomEllerMin(LocalDate fom) {
        return fom != null ? fom : TIDENES_BEGYNNELSE;
    }

    public static LocalDate tomEllerMax(LocalDate tom) {
        return tom != null ? tom : TIDENES_ENDE;
    }
}

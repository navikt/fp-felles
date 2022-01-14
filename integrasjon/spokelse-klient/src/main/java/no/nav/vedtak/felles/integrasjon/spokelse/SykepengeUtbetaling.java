package no.nav.vedtak.felles.integrasjon.spokelse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


public record SykepengeUtbetaling(LocalDate fom, LocalDate tom, BigDecimal grad) {

    public BigDecimal gradScale2() {
        return grad() != null ? grad().setScale(2, RoundingMode.HALF_UP) : null;
    }
}

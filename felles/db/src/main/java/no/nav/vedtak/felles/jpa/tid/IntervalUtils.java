package no.nav.vedtak.felles.jpa.tid;

import java.time.LocalDate;

public class IntervalUtils extends AbstractLocalDateInterval{

    private LocalDate fomDato;
    private LocalDate tomDato;

    public IntervalUtils(LocalDate fomDato, LocalDate tomDato) {
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
        return new IntervalUtils(fomDato, tomDato);
    }
}

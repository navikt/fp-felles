package no.nav.vedtak.felles.jpa.tid;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Hibernate entitet som modellerer et dato intervall.
 */
@Embeddable
public class DatoIntervallEntitet extends AbstractLocalDateInterval {

    @Column(name = "fom")
    private LocalDate fomDato;

    @Column(name = "tom")
    private LocalDate tomDato;

    private DatoIntervallEntitet() {
        // Hibernate
    }

    private DatoIntervallEntitet(LocalDate fomDato, LocalDate tomDato) {
        if (fomDato == null) {
            throw new IllegalArgumentException("Fra og med dato må være satt.");
        }
        if (tomDato == null) {
            throw new IllegalArgumentException("Til og med dato må være satt.");
        }
        if (tomDato.isBefore(fomDato)) {
            throw new IllegalArgumentException("Til og med dato før fra og med dato.");
        }
        this.fomDato = fomDato;
        this.tomDato = tomDato;
    }

    public static DatoIntervallEntitet fraOgMedTilOgMed(LocalDate fomDato, LocalDate tomDato) {
        return new DatoIntervallEntitet(fomDato, tomDato);
    }

    public static DatoIntervallEntitet fraOgMed(LocalDate fomDato) {
        return new DatoIntervallEntitet(fomDato, TIDENES_ENDE);
    }

    public static DatoIntervallEntitet fraOgMedPlusArbeidsdager(LocalDate fom, int antallArbeidsdager) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(fom, finnTomDato(fom, antallArbeidsdager));
    }

    public static DatoIntervallEntitet tilOgMedMinusArbeidsdager(LocalDate tom, int antallArbeidsdager) {
        return DatoIntervallEntitet.fraOgMedTilOgMed(finnFomDato(tom, antallArbeidsdager), tom);
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
    protected DatoIntervallEntitet lagNyPeriode(LocalDate fomDato, LocalDate tomDato) {
        return fraOgMedTilOgMed(fomDato, tomDato);
    }

}

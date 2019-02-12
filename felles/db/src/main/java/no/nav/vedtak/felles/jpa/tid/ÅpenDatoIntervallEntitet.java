package no.nav.vedtak.felles.jpa.tid;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Hibernate entitet som modellerer et dato intervall med nullable til og med dato.
 */
@Embeddable
public class ÅpenDatoIntervallEntitet extends AbstractLocalDateInterval {

    @Column(name = "fom")
    private LocalDate fomDato;

    @Column(name = "tom")
    private LocalDate tomDato;

    private ÅpenDatoIntervallEntitet() {
        // Hibernate
    }

    private ÅpenDatoIntervallEntitet(LocalDate fomDato, LocalDate tomDato) {
        if (fomDato == null && tomDato != null) {
            throw new IllegalArgumentException("Fra og med dato må være satt når til og med dato er satt.");
        }
        if (fomDato != null && tomDato != null && tomDato.isBefore(fomDato)) {
            throw new IllegalArgumentException("Til og med dato er før fra og med dato.");
        }
        this.fomDato = fomDato;
        this.tomDato = tomDato;
    }

    public static ÅpenDatoIntervallEntitet fraOgMedTilOgMed(LocalDate fomDato, LocalDate tomDato) {
        return new ÅpenDatoIntervallEntitet(fomDato, tomDato);
    }

    public static ÅpenDatoIntervallEntitet fraOgMedPlusArbeidsdager(LocalDate fom, int antallArbeidsdager) {
        return ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(fom, finnTomDato(fom, antallArbeidsdager));
    }

    public static ÅpenDatoIntervallEntitet tilOgMedMinusArbeidsdager(LocalDate tom, int antallArbeidsdager) {
        return ÅpenDatoIntervallEntitet.fraOgMedTilOgMed(finnFomDato(tom, antallArbeidsdager), tom);
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
    protected ÅpenDatoIntervallEntitet lagNyPeriode(LocalDate fomDato, LocalDate tomDato) {
        return fraOgMedTilOgMed(fomDato, tomDato);
    }

    @Override
    public String toString() {
        String fom = fomDato != null ? fomDato.format(FORMATTER) : null;
        String tom = tomDato != null ? tomDato.format(FORMATTER) : null;
        return String.format("Periode: %s - %s", fom, tom);
    }
}

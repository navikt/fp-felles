package no.nav.vedtak.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BevegeligeHelligdagerUtil {

    private BevegeligeHelligdagerUtil() {
        //Privat constructor for å hindre instanser.
    }


    /**
     * Henter første virkedag lik eller etter input-datoen.
     * @param dato Fra og med-dato det gjelder
     * @return Første virkedag fra og med dato.
     */
    public static LocalDate hentFørsteVirkedagFraOgMed(final LocalDate dato) {
        List<LocalDate> helligdagerList = finnBevegeligeHelligdagerUtenHelgPerÅr(dato.getYear());
        if (!erDatoHelg(dato) && !helligdagerList.contains(dato)) {
            return dato;
        }
        LocalDate nyDato = dato.plusDays(1);
        while (erDatoHelg(nyDato) || helligdagerList.contains(nyDato)) {
            nyDato = nyDato.plusDays(1);
        }
        return nyDato;
    }

    /**
     * Er dato en helligdag eller helg.
     */
    public static boolean erDatoHelligdagEllerHelg(LocalDate dato) {
        if (erDatoHelg(dato)) {
            return true;
        }
        List<LocalDate> helligdagerList = finnBevegeligeHelligdagerUtenHelgPerÅr(dato.getYear());
        return helligdagerList.contains(dato);
    }

    /**
     * Returnerer en liste med alle helligdager uten helg for alle årene inkludert i perioden som er sendt inn
     */
    public static List<LocalDate> finnBevegeligeHelligdagerUtenHelg(LocalDate fom, LocalDate tom) {
        List<LocalDate> bevegeligeHelligdager = new ArrayList<>();

        for (Integer år : utledÅreneDetSkalFinnesHelligdagerFor(fom, tom)) {
            bevegeligeHelligdager.addAll(finnBevegeligeHelligdagerUtenHelgPerÅr(år));
        }
        return bevegeligeHelligdager;
    }

    /**
     * Returnerer en liste med alle helligdager uten helg for gitt år
     */
    public static List<LocalDate> finnBevegeligeHelligdagerUtenHelgPerÅr(int år) {
        List<LocalDate> bevegeligeHelligdager = new ArrayList<>();

        // legger til de satte helligdagene
        bevegeligeHelligdager.add(LocalDate.of(år, 1, 1));
        bevegeligeHelligdager.add(LocalDate.of(år, 5, 1));
        bevegeligeHelligdager.add(LocalDate.of(år, 5, 17));
        bevegeligeHelligdager.add(LocalDate.of(år, 12, 25));
        bevegeligeHelligdager.add(LocalDate.of(år, 12, 26));

        // regner ut påskedag
        LocalDate påskedag = utledPåskedag(år);

        // søndag før påske; Palmesøndag
        bevegeligeHelligdager.add(påskedag.minusDays(7));

        // torsdag før påske; Skjærtorsdag
        bevegeligeHelligdager.add(påskedag.minusDays(3));

        // fredag før påske; Langfredag
        bevegeligeHelligdager.add(påskedag.minusDays(2));

        // 1.påskedag
        bevegeligeHelligdager.add(påskedag);

        // 2.påskedag
        bevegeligeHelligdager.add(påskedag.plusDays(1));

        // Kristi Himmelfartsdag
        bevegeligeHelligdager.add(påskedag.plusDays(39));

        // 1.pinsedag
        bevegeligeHelligdager.add(påskedag.plusDays(49));

        // 2.pinsedag
        bevegeligeHelligdager.add(påskedag.plusDays(50));

        return fjernHelg(bevegeligeHelligdager);
    }


    private static List<LocalDate> fjernHelg(List<LocalDate> bevegeligeHelligdager) {
        return bevegeligeHelligdager.stream()
                .filter(dato -> dato.getDayOfWeek().getValue() < DayOfWeek.SATURDAY.getValue())
                .sorted()
                .collect(Collectors.toList());
    }

    private static boolean erDatoHelg(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue();
    }

    private static LocalDate utledPåskedag(int år) {
        int a = år % 19;
        int b = år / 100;
        int c = år % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = ((19 * a) + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + (2 * e) + (2 * i) - h - k) % 7;
        int m = (a + (11 * h) + (22 * l)) / 451;
        int n = (h + l - (7 * m) + 114) / 31; // Tallet på måneden
        int p = (h + l - (7 * m) + 114) % 31; // Tallet på dagen

        return LocalDate.of(år, n, p + 1);
    }

    private static List<Integer> utledÅreneDetSkalFinnesHelligdagerFor(LocalDate fom, LocalDate tom) {
        List<Integer> årene = new ArrayList<>();
        int antall = tom.getYear() - fom.getYear();
        for (int i = 0; i <= antall; i++) {
            årene.add(fom.getYear() + i);
        }
        return årene;
    }
}

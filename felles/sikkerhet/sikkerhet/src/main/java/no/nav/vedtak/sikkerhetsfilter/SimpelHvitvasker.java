package no.nav.vedtak.sikkerhetsfilter;

import com.google.common.base.CharMatcher;

@SuppressWarnings("deprecation")
public class SimpelHvitvasker {

    // FIXME (LIBELLE): Bli kvitt Guava avhengiget her

    //Legg merke til at det brukes negativen av matchingen pågrunn av bruk av replace istedet for retain.
    private static CharMatcher kunBokstaverMatcher = CharMatcher.javaLetter().or(CharMatcher.digit()).or(CharMatcher.whitespace()).or(CharMatcher.anyOf(",.-:")).negate();
    private static CharMatcher cookieMatcher = CharMatcher.ascii().and(CharMatcher.anyOf(";, ").negate()).negate();
    private static CharMatcher bokstaverOgVanligeTegnMatcher = CharMatcher.javaLetter().or(CharMatcher.digit()).or(CharMatcher.whitespace()).or(CharMatcher.anyOf("-._=%&*")).negate();

    private SimpelHvitvasker() {
    }


    /**
     * Hvitvasker for alt som ikke er bokstaver
     * Legg merke til at det brukes negativen av matchingen pågrunn av bruk av replace istedet for retain.
     *
     * @param uvasketTekst Tekst som skal vaskes
     * @return ferdig vasket tekst
     */
    public static String hvitvaskKunBokstaver(String uvasketTekst) {
        if (uvasketTekst == null || uvasketTekst.isEmpty()) return uvasketTekst;
        return kunBokstaverMatcher.replaceFrom(uvasketTekst, '_');
    }

    /**
     * Hvitvasker som trolig skal brukes for queryparams og cookies
     * Legg merke til at det brukes negativen av matchingen pågrunn av bruk av replace istedet for retain.
     *
     * @param uvasketTekst Tekst som skal vaskes
     * @return ferdig vasket tekst
     */
    public static String hvitvaskBokstaverOgVanligeTegn(String uvasketTekst) {
        if (uvasketTekst == null || uvasketTekst.isEmpty()) return uvasketTekst;
        return bokstaverOgVanligeTegnMatcher.replaceFrom(uvasketTekst, '_');
    }

    /**
     * Hvitvasker som trolig skal brukes for queryparams og cookies
     * Legg merke til at det brukes negativen av matchingen pågrunn av bruk av replace istedet for retain.
     *
     * @param uvasketTekst Tekst som skal vaskes
     * @return ferdig vasket tekst
     */
    public static String hvitvaskCookie(String uvasketTekst) {
        if (uvasketTekst == null || uvasketTekst.isEmpty()) return uvasketTekst;
        return cookieMatcher.replaceFrom(uvasketTekst, '_');
    }

}

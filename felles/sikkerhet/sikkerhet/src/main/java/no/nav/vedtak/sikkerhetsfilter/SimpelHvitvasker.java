package no.nav.vedtak.sikkerhetsfilter;

import static no.nav.vedtak.log.util.LoggerUtils.removeLineBreaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;

@SuppressWarnings("deprecation")
public class SimpelHvitvasker {

    // FIXME (LIBELLE): Bli kvitt Guava avhengiget her

    //Legg merke til at det brukes negativen av matchingen pågrunn av bruk av replace istedet for retain.
    private static CharMatcher kunBokstaverMatcher = CharMatcher.javaLetter().or(CharMatcher.digit()).or(CharMatcher.whitespace()).or(CharMatcher.anyOf(",.-:")).negate();
    private static CharMatcher cookieMatcher = CharMatcher.ascii().and(CharMatcher.anyOf(";, ").negate()).negate();
    private static CharMatcher bokstaverOgVanligeTegnMatcher = CharMatcher.javaLetter().or(CharMatcher.digit()).or(CharMatcher.whitespace()).or(CharMatcher.anyOf("-._=%&*")).negate();

    private static final Logger log = LoggerFactory.getLogger(SimpelHvitvasker.class);

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
        String rensetTekst = kunBokstaverMatcher.replaceFrom(uvasketTekst, '_');
        if (!uvasketTekst.equals(rensetTekst)) {
            if (log.isInfoEnabled()) {
                log.info(removeLineBreaks("Hvitvasking av kun bokstav tekst: fra '{}' til '{}'"),
                        removeLineBreaks(uvasketTekst), removeLineBreaks(rensetTekst));
            }
        }
        return rensetTekst;
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
        String rensetTekst = bokstaverOgVanligeTegnMatcher.replaceFrom(uvasketTekst, '_');
        if (!uvasketTekst.equals(rensetTekst)) {
            if (log.isInfoEnabled()) {
                log.info(removeLineBreaks("Hvitvasking av kunbokstaver og vanlige tegn: fra '{}' til '{}'"),
                        removeLineBreaks(uvasketTekst), removeLineBreaks(rensetTekst));
            }
        }
        return rensetTekst;
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
        String rensetTekst = cookieMatcher.replaceFrom(uvasketTekst, '_');
        if (!uvasketTekst.equals(rensetTekst)) {
            if (log.isInfoEnabled()) {
                log.info(removeLineBreaks("Hvitvasking av cookie: fra '{}' til '{}'"),
                        removeLineBreaks(uvasketTekst), removeLineBreaks(rensetTekst));
            }
        }
        return rensetTekst;
    }

}

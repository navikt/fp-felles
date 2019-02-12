package no.nav.vedtak.util;

/**
 * Minimalistisk sett med string utils i bruk, slik at en ikke trenger avhengighet på commons-lang3, eller bruke et vilkårlig bibliotek
 */
public final class StringUtils {

    private StringUtils() {
    }
    
    /*
     * Sjekker om en CharSequence er whitespace, tom ("") eller null.
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }
}

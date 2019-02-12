package no.nav.vedtak.util;

/**
 * Minimalistisk sett med supplementære metoder til java.util.Objects. Slipper dermed blandet sett med avhengigheter til
 * Google Guava, Apache commmons-lang.
 */
public final class Objects {
    
    private Objects() {
    }

    /**
     * Validerer tilstand
     * Format på melding tilsvarer String#format
     * @param check gyldig eller ikke
     * @param message exception melding
     * @param params parametere til feilmeldingen
     * @throws IllegalArgumentException kastes dersom check == false.
     */
    public static void check(boolean check, String message, Object... params) {
        if (!check) {
            throw new IllegalArgumentException(String.format(message, params));
        }
    }

    /**
     * Kaster IllegalStateException dersom check == false.
     * Format på melding tilsvarer String#format
     * @param check gyldig eller ikke
     * @param message exception melding
     * @param params parametere til feilmeldingen
     * @throws IllegalArgumentException kastes dersom check == false.
     */
    public static void checkState(boolean check, String message, Object... params) {
        if (!check) {
            throw new IllegalStateException(String.format(message, params));
        }
    }
}

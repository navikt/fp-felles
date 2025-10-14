package no.nav.vedtak.util;


import java.util.regex.Pattern;

public final class StringCaseUtils {

    private static final Pattern SKILLETEGN = Pattern.compile("[\\s()\\-_.,/]");

    private StringCaseUtils() { }

    public static String formaterMedStoreOgSmåBokstaver(String tekst) {
        if (tekst == null || tekst.trim().isEmpty()) {
            return null;
        }
        var tegn = tekst.trim().toLowerCase().toCharArray();
        var nesteSkalHaStorBokstav = true;
        for (var i = 0; i < tegn.length; i++) {
            var erSkilletegn = SKILLETEGN.matcher(String.valueOf(tegn[i])).matches();
            if (!erSkilletegn && nesteSkalHaStorBokstav) {
                tegn[i] = Character.toTitleCase(tegn[i]);
            }
            nesteSkalHaStorBokstav = erSkilletegn;
        }
        return new String(tegn);

    }

}

package no.nav.vedtak.log.mdc;

public class FnrUtils {
    private FnrUtils() {
        // hide construktor
    }

    public static String maskFnr(String userId) {
        if (userId.matches("^\\d{11}$")) {
            return userId.replaceAll("\\d{5}$", "*****");
        }
        return userId;
    }
}

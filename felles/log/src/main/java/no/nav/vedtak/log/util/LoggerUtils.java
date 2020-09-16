package no.nav.vedtak.log.util;

import java.util.Optional;

public final class LoggerUtils {

    private LoggerUtils() {
    }

    public static String removeLineBreaks(String string) {
        return Optional.ofNullable(string)
                .map(s -> s.replaceAll("(\\r|\\n)", ""))
                .orElse(null);
    }

    public static String toStringWithoutLineBreaks(Object object) {
        return Optional.ofNullable(object)
                .map(Object::toString)
                .map(LoggerUtils::removeLineBreaks)
                .orElse(null);
    }
}

package no.nav.vedtak.log.sporingslogg;

import java.util.Map;

import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

@Deprecated(since = "3.1.x", forRemoval = true)
/* Utgår erstattes av auditlog */
public class SporingsloggHelper {
    private static final char SPACE_SEPARATOR = ' ';

    // Pure helper, no instance
    private SporingsloggHelper() {
    }

    public static void logSporingForTask(Class<?> clazz, Sporingsdata sporingsdata, String action) {
        logSporing(clazz, sporingsdata, "task", action);
    }

    public static void logSporing(Class<?> clazz, Map<String, Object> sporingsdata, String actionType, String action) {
        StringBuilder msg = new StringBuilder()
                .append("action=").append(action).append(SPACE_SEPARATOR)
                .append("actionType=").append(actionType).append(SPACE_SEPARATOR);
        for (var entry : sporingsdata.entrySet()) {
            msg.append(entry.getKey()).append('=').append(entry.getValue()).append(SPACE_SEPARATOR);
        }
        String sanitizedMsg = LoggerUtils.toStringWithoutLineBreaks(msg.toString());
        AppLoggerFactory.getSporingLogger(clazz).info(sanitizedMsg); //NOSONAR
    }

    // Legacy
    public static void logSporing(Class<?> clazz, Sporingsdata sporingsdata, String actionType, String action) {
        StringBuilder msg = new StringBuilder()
                .append("action=").append(action).append(SPACE_SEPARATOR)
                .append("actionType=").append(actionType).append(SPACE_SEPARATOR);
        for (var entry : sporingsdata.entrySet()) {
            msg.append(entry.getKey()).append('=').append(entry.getValue()).append(SPACE_SEPARATOR);
        }
        String sanitizedMsg = LoggerUtils.toStringWithoutLineBreaks(msg.toString());
        AppLoggerFactory.getSporingLogger(clazz).info(sanitizedMsg); //NOSONAR
    }
}

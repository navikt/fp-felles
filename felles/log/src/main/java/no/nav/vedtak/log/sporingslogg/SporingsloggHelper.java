package no.nav.vedtak.log.sporingslogg;

import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.util.AppLoggerFactory;

public class SporingsloggHelper {
    private static final char SPACE_SEPARATOR = ' ';

    // Pure helper, no instance
    private SporingsloggHelper() {
    }

    public static void logSporingForTask(Class<?> clazz, Sporingsdata sporingsdata, String action) {
        logSporing(clazz, sporingsdata, "task", action);
    }

    public static void logSporing(Class<?> clazz, Sporingsdata sporingsdata, String actionType, String action) {
        StringBuilder msg = new StringBuilder()
                .append("action=").append(action).append(SPACE_SEPARATOR)
                .append("actionType=").append(actionType).append(SPACE_SEPARATOR);
        for (SporingsloggId id : sporingsdata.getNøkler()) {
            msg.append(id.getEksternKode()).append('=').append(sporingsdata.getVerdi(id)).append(SPACE_SEPARATOR);
        }
        String sanitizedMsg = LoggerUtils.toStringWithoutLineBreaks(msg.toString());
        AppLoggerFactory.getSporingLogger(clazz).info(sanitizedMsg); //NOSONAR
    }
}
package no.nav.vedtak.log.mdc;

import static no.nav.vedtak.log.mdc.FnrUtils.maskFnr;
import static org.slf4j.MDC.get;
import static org.slf4j.MDC.put;

import java.util.Objects;
import java.util.UUID;

import org.slf4j.MDC;

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
public final class MDCOperations {

    private static final String MDC_CALL_ID = "callId";
    private static final String MDC_USER_ID = "userId";
    private static final String MDC_CONSUMER_ID = "consumerId";

    private MDCOperations() {
    }

    public static void putCallId() {
        putCallId(generateCallId());
    }

    public static void putCallId(String callId) {
        toMDC(MDC_CALL_ID, callId);
    }

    public static String getCallId() {
        return get(MDC_CALL_ID);
    }

    public static void removeCallId() {
        remove(MDC_CALL_ID);
    }

    public static void putConsumerId(String consumerId) {
        toMDC(MDC_CONSUMER_ID, consumerId);
    }

    public static String getConsumerId() {
        return get(MDC_CONSUMER_ID);
    }

    public static void removeConsumerId() {
        remove(MDC_CONSUMER_ID);
    }

    public static void putUserId(String userId) {
        Objects.requireNonNull(userId, "userId can't be null");
        put(MDC_USER_ID, maskFnr(userId));
    }

    public static String getUserId() {
        return get(MDC_USER_ID);
    }

    public static void removeUserId() {
        remove(MDC_USER_ID);
    }

    public static String generateCallId() {
        return UUID.randomUUID().toString();
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

    private static void toMDC(String key, Object value) {
        if (value != null) {
            put(key, value.toString());
        }
    }
}

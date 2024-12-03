package no.nav.vedtak.log.mdc;

import static no.nav.vedtak.log.mdc.FnrUtils.maskFnr;
import static org.slf4j.MDC.get;
import static org.slf4j.MDC.put;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.MDC;

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
public final class MDCOperations {
    @Deprecated(forRemoval = true) // Immediate etter tasks ferdig
    private static final String NAV_CALL_ID = "Nav-CallId";
    @Deprecated(forRemoval = true) // Immediate
    private static final String NAV_USER_ID = "Nav-userId";
    @Deprecated(forRemoval = true) // Immediate
    private static final String NAV_CONSUMER_ID = "Nav-ConsumerId";

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
        return Optional.ofNullable(get(MDC_CALL_ID)).orElseGet(() -> get(NAV_CALL_ID));
    }

    public static void removeCallId() {
        remove(NAV_CALL_ID);
        remove(MDC_CALL_ID);
    }

    public static void putConsumerId(String consumerId) {
        toMDC(MDC_CONSUMER_ID, consumerId);
    }

    public static String getConsumerId() {
        return Optional.ofNullable(get(MDC_CONSUMER_ID)).orElseGet(() -> get(NAV_CONSUMER_ID));
    }

    public static void removeConsumerId() {
        remove(NAV_CONSUMER_ID);
        remove(MDC_CONSUMER_ID);
    }

    public static void putUserId(String userId) {
        Objects.requireNonNull(userId, "userId can't be null");
        put(MDC_USER_ID, maskFnr(userId));
    }

    public static String getUserId() {
        return Optional.ofNullable(get(MDC_USER_ID)).orElseGet(() -> get(NAV_USER_ID));
    }

    public static void removeUserId() {
        remove(NAV_USER_ID);
        remove(MDC_USER_ID);
    }

    public static String generateCallId() {
        return CallIdGenerator.create();
    }

    @Deprecated
    public static String getFromMDC(String key) {
        return get(key);
    }

    public static void putToMDC(String key, String value) {
        toMDC(key, value);
    }

    public static void putToMDC(String key, String value, String defaultValue) {
        put(key, Optional.ofNullable(value).orElse(defaultValue));
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

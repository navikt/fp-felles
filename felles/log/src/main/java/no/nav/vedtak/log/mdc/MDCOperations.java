package no.nav.vedtak.log.mdc;

import static org.slf4j.MDC.get;
import static org.slf4j.MDC.put;

import java.util.Objects;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.slf4j.MDC;

/**
 * Utility-klasse for kommunikasjon med MDC. (Knabbet fra modig-log-common)
 */
public final class MDCOperations {
    public static final String HTTP_HEADER_CALL_ID = "Nav-Callid";
    public static final String HTTP_HEADER_CONSUMER_ID = "Nav-Consumer-Id";
    public static final String NAV_CONSUMER_ID = "Nav-ConsumerId";
    public static final String NAV_CALL_ID = "Nav-CallId";
    public static final String NAV_USER_ID = "Nav-userId";

    @Deprecated
    public static final String MDC_CALL_ID = "callId";
    @Deprecated
    public static final String MDC_USER_ID = "userId";
    @Deprecated
    public static final String MDC_CONSUMER_ID = "consumerId";

    // QName for the callId header
    public static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDC_CALL_ID);

    private MDCOperations() {
    }

    public static void putCallId() {
        putCallId(generateCallId());
    }

    public static void putCallId(String callId) {
        toMDC(NAV_CALL_ID, callId);
    }

    public static String getCallId() {
        return get(NAV_CALL_ID);
    }

    public static void removeCallId() {
        remove(NAV_CALL_ID);
    }

    public static void putConsumerId(String consumerId) {
        toMDC(NAV_CONSUMER_ID, consumerId);
    }

    public static String getConsumerId() {
        return get(NAV_CONSUMER_ID);
    }

    public static void removeConsumerId() {
        remove(NAV_CONSUMER_ID);
    }

    public static void putUserId(String userId) {
        Objects.requireNonNull(userId, "userId can't be null");
        put(NAV_USER_ID, maskFnr(userId));
    }

    private static String maskFnr(String userId) {
        if (userId.matches("^\\d{11}$")) {
            return userId.replaceAll("\\d{5}$", "*****");
        }
        return userId;
    }

    public static String getUserId() {
        return get(NAV_USER_ID);
    }

    public static void removeUserId() {
        remove(NAV_USER_ID);
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

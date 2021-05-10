package no.nav.vedtak.log.mdc;

import static org.slf4j.MDC.get;
import static org.slf4j.MDC.put;

import java.util.Objects;
import java.util.Random;

import javax.xml.namespace.QName;

import org.slf4j.MDC;

/**
 * Utility-klasse for kommunikasjon med MDC.
 * (Knabbet fra modig-log-common)
 */
public final class MDCOperations {
    public static final String HTTP_HEADER_CALL_ID = "Nav-Callid";
    public static final String HTTP_HEADER_ALT_CALL_ID = "nav-call-id";
    public static final String HTTP_HEADER_CONSUMER_ID = "Nav-Consumer-Id";

    public static final String MDC_CALL_ID = "callId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_CONSUMER_ID = "consumerId";

    // QName for the callId header
    public static final QName CALLID_QNAME = new QName("uri:no.nav.applikasjonsrammeverk", MDC_CALL_ID);

    private static final Random RANDOM = new Random();

    private MDCOperations() {
    }

    public static void putCallId() {
        putCallId(generateCallId());
    }

    public static void putCallId(String callId) {
        Objects.requireNonNull(callId, "callId can't be null");
        put(MDC_CALL_ID, callId);
    }

    public static void ensureCallId() {
        var callId = getCallId();
        if (callId == null || callId.isBlank()) {
            putCallId(generateCallId());
        }
    }

    public static String getCallId() {
        return get(MDC_CALL_ID);
    }

    public static void removeCallId() {
        remove(MDC_CALL_ID);
    }

    public static void putConsumerId(String consumerId) {
        Objects.requireNonNull(consumerId, "consumerId can't be null");
        put(MDC_CONSUMER_ID, consumerId);
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

    private static String maskFnr(String userId) {
        if (userId.matches("^\\d{11}$")) {
            return userId.replaceAll("\\d{5}$", "*****");
        }
        return userId;
    }

    public static String getUserId() {
        return get(MDC_USER_ID);
    }

    public static void removeUserId() {
        remove(MDC_USER_ID);
    }

    public static String generateCallId() {
        var randomNr = RANDOM.nextInt(Integer.MAX_VALUE);
        var systemTime = System.currentTimeMillis();
        return "CallId_" + systemTime + '_' + randomNr;
    }

    public static String getFromMDC(String key) {
        return MDC.get(key);
    }

    public static void putToMDC(String key, String value) {
        put(key, value);
    }

    public static void remove(String key) {
        MDC.remove(key);
    }

}

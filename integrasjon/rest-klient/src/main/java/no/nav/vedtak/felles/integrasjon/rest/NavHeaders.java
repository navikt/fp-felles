package no.nav.vedtak.felles.integrasjon.rest;

/**
 * Standard NAV header names.
 */
public final class NavHeaders {

    private NavHeaders() {
    }

    // Commonly used call-id not set by default
    public static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
    public static final String HEADER_NAV_CORRELATION_ID = "X-Correlation-ID";

    // Convention to specify person ident
    public static final String HEADER_NAV_PERSONIDENT = "Nav-Personident";
    public static final String HEADER_NAV_PERSONIDENTER = "Nav-Personidenter";

    // Internal: Will be set by RestRequest, no need to set these in clients
    public static final String HEADER_NAV_CALLID = "Nav-Callid";
    public static final String HEADER_NAV_LOWER_CALL_ID = "nav-call-id";
    public static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";
    public static final String HEADER_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";


}

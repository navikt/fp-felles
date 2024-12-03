package no.nav.vedtak.felles.integrasjon.rest;

import no.nav.vedtak.klient.http.CommonHttpHeaders;

/**
 * Standard NAV header names.
 */
public final class NavHeaders {

    private NavHeaders() {
    }

    // Commonly used call-id not set by default, but used by some producers
    public static final String HEADER_NAV_CALL_ID = "Nav-Call-Id";
    public static final String HEADER_NAV_CORRELATION_ID = "X-Correlation-ID";

    // Convention to specify person identifier for som integrations
    public static final String HEADER_NAV_PERSONIDENT = "Nav-Personident";
    public static final String HEADER_NAV_PERSONIDENTER = "Nav-Personidenter";

    // Internal: Will be set by RestRequest, no need to set these in clients
    public static final String HEADER_NAV_CALLID = CommonHttpHeaders.HEADER_NAV_CALLID;
    public static final String HEADER_NAV_LOWER_CALL_ID = CommonHttpHeaders.HEADER_NAV_LOWER_CALL_ID;
    public static final String HEADER_NAV_CONSUMER_ID = CommonHttpHeaders.HEADER_NAV_CONSUMER_ID;

}

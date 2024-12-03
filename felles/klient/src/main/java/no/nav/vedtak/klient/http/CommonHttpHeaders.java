package no.nav.vedtak.klient.http;

/**
 * Standard NAV headere som brukes i kommunikasjon mellom Foreldrepenge-applikasjoner.
 */
public final class CommonHttpHeaders {

    private CommonHttpHeaders() {
    }

    // Headere som brukes mellom FP-applikasjoner
    public static final String HEADER_NAV_CALLID = "Nav-Callid";
    public static final String HEADER_NAV_CONSUMER_ID = "Nav-Consumer-Id";

    // Alternativ header - trengs for noen utgående tilfelle, trengs ikke internt
    public static final String HEADER_NAV_LOWER_CALL_ID = "nav-call-id";

    // Alternativ header - trengs for innkommende kall fra søknad inntil videre. Bør fjernes
    public static final String HEADER_NAV_ALT_CALLID = "Nav-CallId";
}

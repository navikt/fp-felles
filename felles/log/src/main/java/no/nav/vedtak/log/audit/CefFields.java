package no.nav.vedtak.log.audit;

import java.util.Set;

public final class CefFields {

    private static final String BEHANDLING_TEXT = "Behandling";
    private static final String SAKSNUMMER_TEXT = "Saksnummer";

    private CefFields() {}


    public static Set<CefField> forSaksnummer(long saksnummer) {
        return forSaksnummer(Long.toString(saksnummer));
    }

    public static Set<CefField> forSaksnummer(String saksnummer) {
        return Set.of(new CefField(CefFieldName.SAKSNUMMER_VERDI, saksnummer), new CefField(CefFieldName.SAKSNUMMER_LABEL, SAKSNUMMER_TEXT));
    }

    public static Set<CefField> forBehandling(String behandling) {
        return Set.of(new CefField(CefFieldName.BEHANDLING_VERDI, behandling), new CefField(CefFieldName.BEHANDLING_LABEL, BEHANDLING_TEXT));
    }

}

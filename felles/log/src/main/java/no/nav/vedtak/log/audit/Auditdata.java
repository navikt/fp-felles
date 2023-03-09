package no.nav.vedtak.log.audit;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data som utgjør et innslag i sporingsloggen i "Common Event Format (CEF)".
 */
public class Auditdata {

    private static final String FIELD_SEPARATOR = " ";

    private final AuditdataHeader header;
    private final Set<CefField> fields;

    public Auditdata(AuditdataHeader header, Set<CefField> fields) {
        this.header = header;
        this.fields = fields;
    }

    /**
     * Loggstreng i "Commen Event Format (CEF)".
     */
    @Override
    public String toString() {
        return header.toString() + fields.stream()
            .map(CefField::toString)
            .sorted()
            .collect(Collectors.joining(FIELD_SEPARATOR));
    }
}

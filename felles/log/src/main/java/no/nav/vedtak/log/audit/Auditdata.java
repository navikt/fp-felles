package no.nav.vedtak.log.audit;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data som utgjør et innslag i sporingsloggen i "Common Event Format (CEF)".
 */
public class Auditdata {
    
    private static final String FIELD_SEPARATOR = " ";
    
    private AuditdataHeader header;
    private Set<CefField> fields;

    public Auditdata(AuditdataHeader header, Set<CefField> fields) {
        this.header = header;
        this.fields = new HashSet<>(fields);
    }
    
    
    /**
     * Loggstreng i "Commen Event Format (CEF)".
     */
    public String toString() {
        return header.toString() + fields.stream()
                .map(f -> f.toString())
                .collect(Collectors.joining(FIELD_SEPARATOR));
    }
}
    
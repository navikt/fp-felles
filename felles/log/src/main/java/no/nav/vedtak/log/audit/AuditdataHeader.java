package no.nav.vedtak.log.audit;

import java.util.Objects;

@Deprecated(since = "3.1.x", forRemoval = true)
/* Bruk samme fra no.nav.foreldrepenger.felles:log. */
public class AuditdataHeader {

    /**
     * Loggversjon som endres ved nye felter. Ved bytte avtales nytt format med
     * Arcsight-gjengen.
     */
    private static final String logVersion = "1.0";

    private final String vendor;
    private final String product;
    private final EventClassId eventClassId;
    private final String name;
    private final String severity;

    private AuditdataHeader(String vendor, String product, EventClassId eventClassId, String name, String severity) {
        this.vendor = Objects.requireNonNull(vendor);
        this.product = Objects.requireNonNull(product);
        this.eventClassId = Objects.requireNonNull(eventClassId);
        this.name = Objects.requireNonNull(name);
        this.severity = Objects.requireNonNull(severity);
    }

    public String getVendor() {
        return vendor;
    }

    public String getProduct() {
        return product;
    }

    public EventClassId getEventClassId() {
        return eventClassId;
    }

    public String getName() {
        return name;
    }

    public String getSeverity() {
        return severity;
    }

    /**
     * Loggheader i "Commen Event Format (CEF)".
     */
    @Override
    public String toString() {
        return String.format("CEF:0|%s|%s|%s|%s|%s|%s|",
                cefHeaderEscape(vendor),
                cefHeaderEscape(product),
                cefHeaderEscape(logVersion),
                cefHeaderEscape(eventClassId.getCefKode()),
                cefHeaderEscape(name),
                cefHeaderEscape(severity));
    }

    private static final String cefHeaderEscape(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "").replace("\r", "");
    }

    public static final class Builder {
        private String vendor;
        private String product;
        private EventClassId eventClassId;
        private String name;
        private String severity = "INFO";

        public Builder medVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder medProduct(String product) {
            this.product = product;
            return this;
        }

        public Builder medEventClassId(EventClassId eventClassId) {
            this.eventClassId = eventClassId;
            return this;
        }

        public Builder medName(String name) {
            this.name = name;
            return this;
        }

        public Builder medSeverity(String severity) {
            this.severity = severity;
            return this;
        }

        public AuditdataHeader build() {
            return new AuditdataHeader(vendor, product, eventClassId, name, severity);
        }
    }

}

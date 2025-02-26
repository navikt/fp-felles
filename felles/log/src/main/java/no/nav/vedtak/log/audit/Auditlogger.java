package no.nav.vedtak.log.audit;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

/**
 * Auditlogging til Arcsight i Common Event Format (CEF). Dette er en erstatning for sporingslog.
 * <p>
 * Metode for å inkludere dette i prosjektet:
 * <ol>
 *     <li>Legg inn påkrevde parameteere (se konstruktur under).</li>
 *     <li>Undertrykk gammel sporingslogg i logback.xml med {@code <logger level="OFF" name="sporing" additivity="false" /> }</li>
 *     <li>Sett opp "auditLogger" som beskrevet her: https://github.com/navikt/naudit}</li>
 *     <li>Ta kontakt med Arcsight-gruppen for at de skal motta/endre format for loggen som kommer via "audit.nais".</li>
 * </ol>
 */
@Dependent
public class Auditlogger {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("auditLogger");

    private final boolean enabled;
    private final String defaultVendor;
    private final String defaultProduct;

    @Inject
    public Auditlogger(@KonfigVerdi(value = "auditlogger.enabled", defaultVerdi = "true") String enabled,
                       @KonfigVerdi(value = "auditlogger.vendor") String defaultVendor,
                       @KonfigVerdi(value = "auditlogger.product") String defaultProduct) {
        this.enabled = !Boolean.FALSE.toString().equalsIgnoreCase(enabled);
        this.defaultVendor = defaultVendor;
        this.defaultProduct = defaultProduct;
    }


    public void logg(Auditdata auditdata) {
        AUDIT_LOGGER.info(auditdata.toString());
    }


    public boolean auditLogEnabled() {
        return enabled;
    }

    public boolean auditLogDisabled() {
        return !enabled;
    }

    public String getDefaultVendor() {
        return defaultVendor;
    }

    public String getDefaultProduct() {
        return defaultProduct;
    }
}

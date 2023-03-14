package no.nav.vedtak.log.audit;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

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

    private static final Logger auditLogger = LoggerFactory.getLogger("auditLogger");

    private final String defaultVendor;
    private final String defaultProduct;

    @Inject
    public Auditlogger(@KonfigVerdi(value = "auditlogger.vendor") String defaultVendor,
                       @KonfigVerdi(value = "auditlogger.product") String defaultProduct) {

        this.defaultVendor = defaultVendor;
        this.defaultProduct = defaultProduct;
    }


    public void logg(Auditdata auditdata) {
        auditLogger.info(auditdata.toString());
    }


    public String getDefaultVendor() {
        return defaultVendor;
    }

    public String getDefaultProduct() {
        return defaultProduct;
    }
}

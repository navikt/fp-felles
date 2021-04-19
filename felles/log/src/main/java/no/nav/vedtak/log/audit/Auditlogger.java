package no.nav.vedtak.log.audit;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;

/**
 * Auditlogging til Arcsight i Common Event Format (CEF). Dette er en erstatning for sporingslog.
 *
 * Metode for å inkludere dette i prosjektet:
 * <ol>
 *     <li>Legg inn påkrevde parameteere (se konstruktur under).</li>
 *     <li>Undertrykk gammel sporingslogg i logback.xml med {@code <logger level="OFF" name="sporing" additivity="false" /> }</li>
 *     <li>Sett opp "auditLogger" som beskrevet her: https://github.com/navikt/naudit}</li>
 *     <li>Ta kontakt med Arcsight-gruppen for at de skal motta/endre format for loggen som kommer via "audit.nais".</li>
 * </ol>
 */
@Deprecated(since = "3.1.x", forRemoval = true)
/* Bruk samme fra no.nav.foreldrepenger.felles:log. */
@Dependent
public class Auditlogger {

    private static final Logger auditLogger = LoggerFactory.getLogger("auditLogger");

    private final boolean enabled;
    private final String defaultVendor;
    private final String defaultProduct;

    @Inject
    public Auditlogger(@KonfigVerdi(value = "auditlogger.enabled", required = false) boolean enabled,
            @KonfigVerdi(value = "auditlogger.vendor", required = false) String defaultVendor,
            @KonfigVerdi(value = "auditlogger.product", required = false) String defaultProduct) {

        if (enabled) {
            Objects.requireNonNull(defaultVendor, "defaultVendor == null");
            Objects.requireNonNull(defaultProduct, "defaultProduct == null");
        }
        /*
         * TODO(jol) Re-enable som warning etter flytt av apps til Auditlogger + Warning i SporingsloggHelper + fjerne Kibana-filtre
         * else { LOG.warn("Denne applikasjonen bruker sporingslogg som har blitt deprecated. Bytt til bruk av \"no.nav.vedtak.log.audit.Auditlogger\"."); }
         *
         */

        this.enabled = enabled;
        this.defaultVendor = defaultVendor;
        this.defaultProduct = defaultProduct;
    }


    public void logg(Auditdata auditdata) {
        if (enabled) {
            auditLogger.info(auditdata.toString());
        }
    }


    public String getDefaultVendor() {
        return defaultVendor;
    }

    public String getDefaultProduct() {
        return defaultProduct;
    }


    public boolean isEnabled() {
        return enabled;
    }
}

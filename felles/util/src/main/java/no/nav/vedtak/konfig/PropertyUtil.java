package no.nav.vedtak.konfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

public class PropertyUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyUtil.class);

    private static final Environment ENV = Environment.current();

    private PropertyUtil() {
    }

    /**
     *
     * @deprecated Bruk heller {@link Environment#getProperty(String)} direkte
     * 
     */
    @Deprecated
    public static String getProperty(String key) {
        String fraEnv = ENV.getProperty(key);
        String val = System.getProperty(key);
        if (val == null) {
            val = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        if (fraEnv != val) {
            LOG.warn("Fikk {} fra ENV, {} på gamlemåten, må undersøkes", fraEnv, val);
            return val; // gammel logikk vinner mens vi undersøker hvorfor.
        }
        return fraEnv;
    }
}

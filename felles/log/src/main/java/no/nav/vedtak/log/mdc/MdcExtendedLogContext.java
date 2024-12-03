package no.nav.vedtak.log.mdc;

import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.MDC;

/**
 * {@link MDC} backed parameter med felles prefix (kan være tom)
 */
public class MdcExtendedLogContext {

    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[\\[\\];=]");
    private final String prefix; // May be empty string, but not null

    public MdcExtendedLogContext(String prefix) {
        Objects.requireNonNull(prefix, "paramName");
        this.prefix = prefix;
    }

    public static MdcExtendedLogContext getContext(String kontekstPrefix) {
        return new MdcExtendedLogContext(kontekstPrefix);
    }

    public void add(String key, Object value) {
        validateKey(key);
        if (value == null) {
            MDC.remove(paramKey(key));
        } else {
            MDC.put(paramKey(key), value.toString());
        }
    }

    public void remove(String key) {
        validateKey(key);
        MDC.remove(paramKey(key));
    }

    public String get(String key) {
        validateKey(key);
        return MDC.get(paramKey(key));
    }

    private static void validateKey(String key) {
        if (key == null || ILLEGAL_CHARS.matcher(key).find()) {
            throw new IllegalArgumentException("Ugyldig key: '" + key + "'");
        }
    }

    private String paramKey(String key) {
        return prefix + "_" + key;
    }

}

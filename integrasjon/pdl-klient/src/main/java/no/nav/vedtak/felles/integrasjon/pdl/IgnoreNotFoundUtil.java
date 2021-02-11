package no.nav.vedtak.felles.integrasjon.pdl;

import static org.apache.http.HttpStatus.SC_NOT_FOUND;

import java.util.function.Supplier;

class IgnoreNotFoundUtil {

    private IgnoreNotFoundUtil() {

    }

    static <V> V exec(Supplier<V> supplier, boolean ignoreNotFound) {
        return exec(supplier, ignoreNotFound, null);
    }

    static <V> V exec(Supplier<V> supplier, boolean ignoreNotFound, V defaultValue) {
        try {
            return supplier.get();
        } catch (PdlException e) {
            if (SC_NOT_FOUND == e.getStatus() && ignoreNotFound) {
                return defaultValue;
            }
            throw e;
        }
    }
}

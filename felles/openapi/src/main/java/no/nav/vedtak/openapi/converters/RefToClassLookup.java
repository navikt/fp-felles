package no.nav.vedtak.openapi.converters;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RefToClassLookup {
    private final Map<String, Class<?>> refToClassMap = new ConcurrentHashMap<>();

    public void put(final String ref, final Class<?> cls) {
        this.refToClassMap.put(ref, cls);
    }

    public Class<?> get(final String ref) {
        return this.refToClassMap.get(ref);
    }
}

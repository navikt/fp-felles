package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PdpRequest {

    private final Map<String, Object> attributeMap;

    public PdpRequest() {
        this(new HashMap<>());
    }

    PdpRequest(Map<String, Object> attributeMap) {
        this.attributeMap = attributeMap;
    }

    public void put(String key, Object value) {
        Objects.requireNonNull(key, "Key must not be null");
        attributeMap.put(key, value);
    }

    public Object get(String key) {
        Objects.requireNonNull(key, "Key must not be null");
        return attributeMap.get(key);
    }

    public String getString(String key) {
        Objects.requireNonNull(key, "Key must not be null");
        return (String) attributeMap.get(key);
    }

    public Optional<String> getOptional(String key) {
        return Optional.ofNullable(getString(key));
    }

    @SuppressWarnings("unchecked")
    public List<String> getListOfString(String key) {
        Objects.requireNonNull(key, "Key must not be null");
        if (attributeMap.containsKey(key)) {
            return new ArrayList<>((Collection<? extends String>) attributeMap.get(key));
        }
        return Collections.emptyList();
    }

    public int getAntall(String key) {
        return getListOfString(key).size();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [attributeMap=" + attributeMap + "]";
    }

}

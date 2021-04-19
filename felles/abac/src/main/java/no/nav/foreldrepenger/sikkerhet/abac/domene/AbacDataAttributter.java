package no.nav.foreldrepenger.sikkerhet.abac.domene;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AbacDataAttributter {

    private Map<AbacAttributtType, Set<Object>> attributter = new LinkedHashMap<>();

    public static AbacDataAttributter opprett() {
        return new AbacDataAttributter();
    }

    public AbacDataAttributter leggTil(AbacDataAttributter annen) {
        for (Map.Entry<AbacAttributtType, Set<Object>> entry : annen.attributter.entrySet()) {
            if (entry.getValue() != null) {
                leggTil(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public Set<AbacAttributtType> keySet() {
        return attributter.keySet();
    }

    public AbacDataAttributter leggTil(AbacAttributtType type, Collection<Object> samling) {
        Set<Object> a = attributter.get(type);
        if (a == null) {
            attributter.put(type, new LinkedHashSet<>(samling));
        } else {
            a.addAll(samling);
        }
        return this;
    }

    public AbacDataAttributter leggTil(AbacAttributtType type, Object verdi) {
        requireNonNull(verdi, "Attributt av type " + type + " kan ikke være null"); //$NON-NLS-1$ //$NON-NLS-2$
        Set<Object> a = attributter.computeIfAbsent(type, k -> new LinkedHashSet<>(4));
        // det er vanligvis bare 1 attributt i settet
        a.add(verdi);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getVerdier(AbacAttributtType type) {
        return attributter.containsKey(type)
            ? (Set<T>) attributter.get(type) // NOSONAR cast fungerer når settere/gettere er symmetriske slik de skal være her
            : Collections.emptySet();
    }

    @Override
    public String toString() {
        return AbacDataAttributter.class.getSimpleName() + "{" +
            attributter.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getKey().getMaskerOutput() ? maskertEllerTom(e.getValue()) : e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbacDataAttributter)) {
            return false;
        }
        AbacDataAttributter annen = (AbacDataAttributter) o;
        return Objects.equals(attributter, annen.attributter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributter);
    }

    private static String maskertEllerTom(Collection<?> input) {
        return input.isEmpty() ? "[]" : "[MASKERT#" + input.size() + "]";
    }
}

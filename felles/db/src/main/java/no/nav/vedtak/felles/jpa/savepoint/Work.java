package no.nav.vedtak.felles.jpa.savepoint;

/**
 * Functional interface som representerer et stykke arbeid. Kunne vært en
 * {@link Callable} men skilt ut for å synliggjøre hvor det brukes.
 */
@FunctionalInterface
public interface Work<V> {
    V doWork();
}
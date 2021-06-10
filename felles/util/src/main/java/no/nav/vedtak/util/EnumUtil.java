package no.nav.vedtak.util;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class EnumUtil {

    private EnumUtil() {

    }

    public static <E extends Enum<E>> E matchOrThrow(Class<E> type, Predicate<? super E> p) {
        return matchOrThrow(type, p, () -> new IllegalArgumentException("Fant ingen match for predikat " + p));
    }

    public static <E extends Enum<E>> E matchOrThrow(Class<E> type, Predicate<? super E> p, Supplier<? extends RuntimeException> exceptionSupplier) {
        return find(type, p).orElseThrow(exceptionSupplier);
    }

    public static <E extends Enum<E>> E match(Class<E> type, Predicate<? super E> p) {
        return match(type, p, (E) null);
    }

    public static <E extends Enum<E>> E match(Class<E> type, Predicate<? super E> p, E defaultValue) {
        return find(type, p)
                .orElse(defaultValue);
    }

    private static <E extends Enum<E>> Optional<E> find(Class<E> type, Predicate<? super E> p) {
        return EnumSet.allOf(type)
                .stream()
                .filter(p)
                .findFirst();
    }
}

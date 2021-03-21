package no.nav.vedtak.util;

import java.util.EnumSet;
import java.util.function.Predicate;

public final class EnumUtil {

    private EnumUtil() {

    }

    public static <E extends Enum<E>> E match(Class<E> type, Predicate<? super E> p) {
        return match(type, p, null);
    }

    public static <E extends Enum<E>> E match(Class<E> type, Predicate<? super E> p, E defaultValue) {
        return EnumSet.allOf(type)
                .stream()
                .filter(p)
                .findFirst()
                .orElse(defaultValue);

    }

}

package no.nav.vedtak.util;

import no.nav.vedtak.exception.VLException;

import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtils {

    private StreamUtils() {

    }

    public static <T> Collector<T, ?, T> toSingleton(Supplier<? extends VLException> exceptionSupplier) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw exceptionSupplier.get();
                    }
                    return list.get(0);
                });
    }
}

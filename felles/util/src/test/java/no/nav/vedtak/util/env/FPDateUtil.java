package no.nav.vedtak.util.env;

import java.time.Clock;
import java.time.LocalDate;

public class FPDateUtil {

    private final Clock clock;

    public FPDateUtil(Clock clock) {
        this.clock = clock;
    }

    public static LocalDate now() {
        return LocalDate.now();
    }
}

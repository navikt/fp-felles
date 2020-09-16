package no.nav.vedtak.felles.testutilities;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import no.nav.vedtak.util.FPDateUtil;

/**
 * JUnit Rule for å stille tiden til en bestemt tid (offset). Resetter etter at
 * testen er kjørt. Kan også endre tiden i løpet av testen for å simulere at
 * tiden løper fortere.
 *
 * @deprecated Fjernes, FPDateUtil er ikke i tilstrekkelig bruk og gir p.t. ikke
 *             mening (kan heller stille tid på docker/verdikjede nivå
 */
@Deprecated(forRemoval = true, since = "2.2.x")
public class StillTid implements MethodRule {

    private final AtomicReference<LocalDateTime> tidRef = new AtomicReference<>();

    public StillTid offsetDager(final int offsetDager) {
        return medTid(LocalDateTime.now().plusDays(offsetDager));
    }

    public StillTid medTid(final LocalDateTime tid) {
        this.tidRef.set(tid);
        return this;
    }

    public StillTid medDag(final LocalDate dag) {
        return medTid(dag.atStartOfDay());
    }

    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        if (tidRef.get() == null) {
            throw new IllegalStateException("Mangler offset tidRef, kan ikke stille klokken.  Har du glemt å kalle en metode?");
        } else {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    FPDateUtil.init(new MyClockProvider(tidRef));
                    try {
                        base.evaluate();
                    } finally {
                        FPDateUtil.init();
                    }
                }

            };
        }
    }

    public static final class MyClockProvider implements FPDateUtil.ClockProvider {

        private AtomicReference<LocalDateTime> tid;

        public MyClockProvider(AtomicReference<LocalDateTime> tid) {
            this.tid = tid;
        }

        @Override
        public Clock getClock() {
            return Clock.offset(Clock.systemDefaultZone(), Duration.ofMinutes(ChronoUnit.MINUTES.between(LocalDateTime.now(), tid.get())));
        }

    }

}

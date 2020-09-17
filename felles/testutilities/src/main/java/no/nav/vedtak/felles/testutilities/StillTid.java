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
 * tiden løper fortere. Denne brukes primært som en tidsmaskin i enkelte tester
 * for å forsikre seg om at {@link LocalDate#now} & friends returnerer en tid i
 * framtiden. Dette kan oppnås på mange andre måter, feks med {@link FixedClock}
 * Man kan også bruke <i>mockito-inline</i> for å mocke {@link LocalDate#now}
 * som under. Hver gang man finenr at man trenger å mocke statiske metoder bør
 * man imidlertid vurdere om disse hjelpeklassene/metodene kan erstattes med en
 * "ordentlig" objekt
 *
 * <pre>
 * &#64;Test
 * public void mockTest() {
 *     var nyttår = LocalDate.of(2000, 1, 1);
 *     try (var mock = mockStatic(FPDateUtil.class)) {
 *         mock.when(FPDateUtil::iDag).thenReturn(nyttår);
 *         var now = FPDateUtil.iDag();
 *         assertEquals(nyttår, now);
 *         mock.verify(FPDateUtil::iDag);
 *     }
 *     assertNotEquals(nyttår, FPDateUtil.iDag());
 * }
 * </pre>
 *
 * @see FixedClock
 *
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

package no.nav.vedtak.felles.integrasjon.jms.pausing;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.modig.core.test.LogSniffer;

public class DefaultErrorHandlingStrategyTest {

    private DefaultErrorHandlingStrategy strategy; // the object we're testing

    @Rule
    public final LogSniffer logSniffer = new LogSniffer();

    @Before
    public void setup() {
        strategy = new DefaultErrorHandlingStrategy();
    }

    @Test
    public void test_handleExceptionOnCreateContext() {
        Exception e = new RuntimeException("oida");
        doAndAssertPause(() -> strategy.handleExceptionOnCreateContext(e));
        logSniffer.assertHasErrorMessage("F-158357");
    }

    @Test
    public void test_handleUnfulfilledPrecondition() {
        doAndAssertPause(() -> strategy.handleUnfulfilledPrecondition("test_handleUnfulfilledPrecondition"));
        logSniffer.assertHasErrorMessage("F-310549");
    }

    @Test
    public void test_handleExceptionOnReceive() {
        Exception e = new RuntimeException("uffda");
        doAndAssertPause(() -> strategy.handleExceptionOnReceive(e));
        logSniffer.assertHasErrorMessage("F-266229");
    }

    @Test
    public void test_handleExceptionOnHandle() {
        Exception e = new RuntimeException("auda");
        doAndAssertPause(() -> strategy.handleExceptionOnHandle(e));
        logSniffer.assertHasErrorMessage("F-848912");
    }

    private void doAndAssertPause(Runnable pausingAction) {
        long before = System.currentTimeMillis();
        pausingAction.run();
        long after = System.currentTimeMillis();
        long actualPause = after - before;
        assertThat(actualPause).isGreaterThanOrEqualTo(2000L);
    }
}

package no.nav.vedtak.felles.integrasjon.jms.pausing;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultErrorHandlingStrategyTest {

    private DefaultErrorHandlingStrategy strategy; // the object we're testing

    @BeforeEach
    public void setup() {
        strategy = new DefaultErrorHandlingStrategy();
    }

    @Test
    public void test_handleExceptionOnCreateContext() {
        Exception e = new RuntimeException("oida");
        doAndAssertPause(() -> {
            var stdout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(stdout));
            strategy.handleExceptionOnCreateContext(e);
            assertTrue(stdout.toString().contains("F-158357"));
        });
    }

    @Test
    public void test_handleUnfulfilledPrecondition() {
        doAndAssertPause(() -> {
            var stdout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(stdout));
            strategy.handleUnfulfilledPrecondition("test_handleUnfulfilledPrecondition");
            assertTrue(stdout.toString().contains("F-310549"));
        });
    }

    @Test
    public void test_handleExceptionOnReceive() {
        Exception e = new RuntimeException("uffda");
        doAndAssertPause(() -> {
            var stdout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(stdout));
            strategy.handleExceptionOnReceive(e);
            assertTrue(stdout.toString().contains("F-266229"));
        });
    }

    @Test
    public void test_handleExceptionOnHandle() {
        Exception e = new RuntimeException("auda");
        doAndAssertPause(() -> {
            var stdout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(stdout));
            strategy.handleExceptionOnHandle(e);
            assertTrue(stdout.toString().contains("F-848912"));
        });
    }

    private static void doAndAssertPause(Runnable pausingAction) {
        long before = System.currentTimeMillis();
        pausingAction.run();
        long after = System.currentTimeMillis();
        long actualPause = after - before;
        assertThat(actualPause).isGreaterThanOrEqualTo(2000L);

    }
}

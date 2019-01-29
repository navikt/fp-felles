package no.nav.vedtak.felles.integrasjon.jms.precond;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Test;

public class AlwaysTruePreconditionCheckerTest {

    @Test
    public void test_isFulfilled() {
        AlwaysTruePreconditionChecker checker = new AlwaysTruePreconditionChecker();

        PreconditionCheckerResult checkerResult = checker.check();
        assertThat(checkerResult.isFulfilled()).isTrue();
        assertThat(checkerResult.getErrorMessage().isPresent()).isFalse();
    }
}

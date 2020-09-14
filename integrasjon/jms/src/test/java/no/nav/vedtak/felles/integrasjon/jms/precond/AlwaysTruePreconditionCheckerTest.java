package no.nav.vedtak.felles.integrasjon.jms.precond;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;

public class AlwaysTruePreconditionCheckerTest {

    @Test
    public void test_isFulfilled() {
        var checkerResult = new AlwaysTruePreconditionChecker().check();
        assertThat(checkerResult.isFulfilled()).isTrue();
        assertThat(checkerResult.getErrorMessage().isPresent()).isFalse();
    }
}

package no.nav.vedtak.felles.integrasjon.jms.pausing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LinearBackoffHandlerTest {
    @Test
    public void skalBrukeAllePauseLengderOgSaaRepetereSistePauseLengde() {

        ErrorHandler backoffHandler;

        backoffHandler = new LinearBackoffHandler(5, 5);

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(5);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(10);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(15);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(20);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(25);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);

        backoffHandler = new NoBackoffHandler();

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(0);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(0);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(0);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(0);
    }

    @Test
    public void skalGaaTilFoerstePauseLengdeVedReset() {

        LinearBackoffHandler backoffHandler;

        backoffHandler = new LinearBackoffHandler(5, 5);

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(5);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(10);

        backoffHandler.reset();

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(5);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(10);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(15);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(20);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(25);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);

        backoffHandler.reset();

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(5);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(10);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(15);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(20);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(25);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(30);

        backoffHandler = new LinearBackoffHandler(3, 0);

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(3);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(3);

        backoffHandler.reset();

        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(3);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(3);
        assertThat(backoffHandler.getNextPauseLengthInMillisecs()).isEqualTo(3);
    }
}
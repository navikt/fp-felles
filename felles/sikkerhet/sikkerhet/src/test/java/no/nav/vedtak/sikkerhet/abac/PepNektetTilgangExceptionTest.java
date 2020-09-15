package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.FeilFactory;

public class PepNektetTilgangExceptionTest {

    @Test
    public void skal_logge_uten_stacktrace_da_det_bare_skaper_støy() throws Exception {
        VLException e = FeilFactory.create(PepFeil.class).ikkeTilgang().toException();
        assertThat(e).isInstanceOf(PepNektetTilgangException.class);
        Logger logger = Mockito.mock(Logger.class);

        // act
        e.log(logger);

        // assert
        Mockito.verify(logger).info("F-608625:Ikke tilgang"); // for stacktrace er det .info(String, Throwable) som gjelder
        Mockito.verifyNoMoreInteractions(logger);

    }
}
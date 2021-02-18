package no.nav.vedtak.sikkerhet.abac;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.VLException;

public class PepNektetTilgangExceptionTest {

    @Test
    public void skal_logge_uten_stacktrace_da_det_bare_skaper_støy() throws Exception {
        VLException e = PepFeil.ikkeTilgang();
        assertThat(e).isInstanceOf(PepNektetTilgangException.class);
    }
}
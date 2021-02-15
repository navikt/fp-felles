package no.nav.vedtak.feil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;

class FeilTest {

    private static final Logger logger = LoggerFactory.getLogger(FeilTest.class);
    private static final String FEIL_KODE = "DUMMY_FEIL_KODE";

    @Test
    void skal_kunne_konvertere_feil_til_exception() {
        Feil feil = new Feil(FEIL_KODE, "noe gikk galt", LogLevel.ERROR, TekniskException.class, null);
        VLException exception = feil.toException();
        assertThat(exception.getMessage()).isEqualTo(feil.toLogString());
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void skal_kunne_konvertere_feil_til_exception_og_ta_med_cause() {
        RuntimeException cause = new RuntimeException("Væææ!");
        Feil feil = new Feil(FEIL_KODE, "noe gikk galt", LogLevel.ERROR, TekniskException.class, cause);
        VLException exception = feil.toException();
        assertThat(exception.getMessage()).isEqualTo(feil.toLogString());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull().isNotEmpty();
    }

    @Test
    void skal_kunne_konvertere_funksjonell_feil_til_funksjonell_exception() {
        RuntimeException cause = new RuntimeException("Væææ!");
        Feil feil = new FunksjonellFeil(FEIL_KODE, "funksjonellFeil", "test", LogLevel.WARN, FunksjonellException.class, cause);
        assertThat(feil.getLogLevel()).isSameAs(Level.WARN);
        VLException exception = feil.toException();
        exception.log(logger);
        assertThat(exception.getMessage()).isEqualTo(feil.toLogString());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull().isNotEmpty();
        assertThat(exception.getFeil().getKode()).isEqualTo(FEIL_KODE);
    }

    @Test
    void skal_ikke_kunne_konvertere_feil_til_funksjonell_exception() {
        RuntimeException cause = new RuntimeException("Væææ!");
        Feil feil = new Feil(FEIL_KODE, "funksjonellFeil", LogLevel.WARN, FunksjonellException.class, cause);
        assertThrows(IllegalStateException.class, () -> feil.toException());
    }

    @Test
    void skal_støtte_feil_med_loglevel_INFO() {
        RuntimeException cause = new RuntimeException("Væææ!");
        Feil feil = new Feil(FEIL_KODE, "integrasjonFeil", LogLevel.INFO, IntegrasjonException.class, cause);
        assertThat(feil.getLogLevel()).isSameAs(Level.INFO);
        VLException exception = feil.toException();
        exception.log(logger);
        assertThat(exception.getMessage()).isEqualTo(feil.toLogString());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull().isNotEmpty();
        assertThat(exception.getFeil().getKode()).isEqualTo(FEIL_KODE);
    }

    @Test
    void skal_kunne_konvertere_feil_til_mangler_tilgang_exception() {
        RuntimeException cause = new RuntimeException("Væææ!");
        Feil feil = new Feil(FEIL_KODE, "manglerTilgangFeil", LogLevel.ERROR, ManglerTilgangException.class, cause);
        assertThat(feil.getLogLevel()).isSameAs(Level.ERROR);
        VLException exception = feil.toException();
        exception.log(logger);
        assertThat(exception.toString()).isNotEmpty();
        assertThat(exception.getMessage()).isEqualTo(feil.toLogString());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getStackTrace()).isNotNull().isNotEmpty();
        assertThat(exception.getFeil().getKode()).isEqualTo(FEIL_KODE);
    }
}

package no.nav.vedtak.feil;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

class FeilFactoryTest {

    @Test
    void skal_generere_teknisk_feil_basert_på_annoteringer_og_parametre_og_exception() throws Exception {
        Exception cause = new IOException("Klarte ikke å mounte filsystem");
        Feil feil = FeilFactory.create(TestFeil.class).kritiskOppstartsfeil("foo", cause);
        assertThat(feil.getKode()).isEqualTo("TestFeil-1");
        assertThat(feil.getLogLevel()).isEqualTo(Level.ERROR);
        assertThat(feil.getFeilmelding()).isEqualTo("Kunne ikke starte delsystem foo");
        assertThat(feil.getCause()).isEqualTo(cause);
    }

    @Test
    void skal_generere_integrasjonfeil_basert_på_annoteringer_og_parametre() throws Exception {
        Feil feil = FeilFactory.create(TestFeil.class).tpsTimeout(30);
        assertThat(feil.getKode()).isEqualTo("TestFeil-3");
        assertThat(feil.getLogLevel()).isEqualTo(Level.WARN);
        assertThat(feil.getFeilmelding()).isEqualTo("TPS svarte ikke (timeout=30 sekunder)");
    }

    @Test
    void skal_generere_ikkeTilgangFeil() throws Exception {
        Feil feil = FeilFactory.create(TestFeil.class).ikkeTilgang();
        assertThat(feil.getKode()).isEqualTo("TestFeil-6");
        assertThat(feil.getLogLevel()).isEqualTo(Level.INFO);
        assertThat(feil.getFeilmelding()).isEqualTo("Ikke lov");
    }

    @Test
    void skal_generere_funksjonell_feil_basert_på_annoteringer_og_parametre() throws Exception {
        Feil feil = FeilFactory.create(TestFeil.class).manglerFødselsvilkår(1337);
        assertThat(feil.getKode()).isEqualTo("TestFeil-2");
        assertThat(feil.getLogLevel()).isEqualTo(Level.WARN);
        assertThat(feil.getFeilmelding()).isEqualTo("Søknaden (saks-id=1337) kan ikke godkjennes før termindato er satt");

        assertThat(feil).isInstanceOf(FunksjonellFeil.class);
        assertThat(((FunksjonellFeil) feil).getLøsningsforslag()).isEqualTo("Bestill termindato og sett sak på vent.");
    }

    @Test
    void skal_ta_med_parameter_som_cause_når_det_bare_er_ett_parameter_og_det_er_en_exception() throws Exception {
        IllegalArgumentException cause = new IllegalArgumentException("#5235");
        Feil feil = FeilFactory.create(TestFeil.class).feilArgument(cause);
        assertThat(feil.getCause()).isEqualTo(cause);

    }

    interface TestFeil extends DeklarerteFeil {

        @TekniskFeil(feilkode = "TestFeil-1", logLevel = LogLevel.ERROR, feilmelding = "Kunne ikke starte delsystem %s")
        Feil kritiskOppstartsfeil(String delsystem, Exception cause);

        @no.nav.vedtak.feil.deklarasjon.FunksjonellFeil(feilkode = "TestFeil-2", logLevel = LogLevel.WARN, feilmelding = "Søknaden (saks-id=%s) kan ikke godkjennes før termindato er satt", løsningsforslag = "Bestill termindato og sett sak på vent.")
        Feil manglerFødselsvilkår(Integer saksId);

        @IntegrasjonFeil(feilkode = "TestFeil-3", logLevel = LogLevel.WARN, feilmelding = "TPS svarte ikke (timeout=%s sekunder)")
        Feil tpsTimeout(Integer timeoutSekunder);

        @TekniskFeil(feilkode = "TestFeil-5", logLevel = LogLevel.ERROR, feilmelding = "Argumentet var helt feil")
        Feil feilArgument(IllegalArgumentException cause);

        @ManglerTilgangFeil(feilkode = "TestFeil-6", logLevel = LogLevel.INFO, feilmelding = "Ikke lov")
        Feil ikkeTilgang();
    }

}
package no.nav.vedtak.server.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import ch.qos.logback.classic.Level;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.FeilType;
import no.nav.vedtak.log.util.MemoryAppender;

@Execution(ExecutionMode.SAME_THREAD)
class GeneralRestExceptionMapperTest {

    private static MemoryAppender logSniffer;

    private final GeneralRestExceptionMapper exceptionMapper = new GeneralRestExceptionMapper();

    @BeforeEach
    void setUp() {
        logSniffer = MemoryAppender.sniff(GeneralRestExceptionMapper.class);
    }

    @AfterEach
    void afterEach() {
        logSniffer.reset();
    }

    @Test
    void skalIkkeMappeManglerTilgangFeil() {
        try (var response = exceptionMapper.toResponse(manglerTilgangFeil())) {

            assertThat(response.getStatus()).isEqualTo(403);
            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feiltype()).isEqualTo(FeilType.MANGLER_TILGANG_FEIL.name());
            assertThat(feilDto.feilmelding()).contains("ManglerTilgangFeilmeldingKode");
            assertThat(logSniffer.search("ManglerTilgangFeilmeldingKode", Level.WARN)).isEmpty();
        }
    }

    @Test
    void skalMappeFunksjonellFeil() {
        try (var response = exceptionMapper.toResponse(funksjonellFeil())) {

            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feilmelding()).contains("FUNK_FEIL");
            assertThat(feilDto.feilmelding()).contains("en funksjonell feilmelding");
            assertThat(feilDto.feilmelding()).contains("et løsningsforslag");
            assertThat(logSniffer.search("en funksjonell feilmelding", Level.WARN)).hasSize(1);
        }
    }

    @Test
    void skalMappeVLException() {
        try (var response = exceptionMapper.toResponse(tekniskFeil())) {

            assertThat(response.getStatus()).isEqualTo(500);

            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feilmelding()).contains("TEK_FEIL");
            assertThat(feilDto.feilmelding()).contains("en teknisk feilmelding");
            assertThat(logSniffer.search("en teknisk feilmelding", Level.WARN)).hasSize(1);
        }
    }

    @Test
    void skalMappeWrappedGenerellFeil() {
        var feilmelding = "en helt generell feil";
        var generellFeil = new RuntimeException(feilmelding);

        try (var response = exceptionMapper.toResponse(new TekniskException("KODE", "TEKST", generellFeil))) {

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feilmelding()).contains("TEKST");
            assertThat(logSniffer.search("TEKST", Level.WARN)).hasSize(1);
        }
    }

    @Test
    void skalMappeWrappedFeilUtenCause() {
        var feilmelding = "en helt generell feil";

        try (var response = exceptionMapper.toResponse(new TekniskException("KODE", feilmelding))) {

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feilmelding()).contains(feilmelding);
            assertThat(logSniffer.search(feilmelding, Level.WARN)).hasSize(1);
        }
    }


    @Test
    void skalMappeGenerellFeil() {
        var feilmelding = "en helt generell feil";
        RuntimeException generellFeil = new IllegalArgumentException(feilmelding);

        try (var response = exceptionMapper.toResponse(generellFeil)) {

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
            var feilDto = (FeilDto) response.getEntity();

            assertThat(feilDto.feilmelding()).contains(feilmelding);
            assertThat(logSniffer.search(feilmelding, Level.WARN)).hasSize(1);
        }
    }


    private static FunksjonellException funksjonellFeil() {
        return new FunksjonellException("FUNK_FEIL", "en funksjonell feilmelding", "et løsningsforslag");
    }

    private static TekniskException tekniskFeil() {
        return new TekniskException("TEK_FEIL", "en teknisk feilmelding");
    }

    private static ManglerTilgangException manglerTilgangFeil() {
        return new ManglerTilgangException("MANGLER_TILGANG_FEIL","ManglerTilgangFeilmeldingKode");
    }

}

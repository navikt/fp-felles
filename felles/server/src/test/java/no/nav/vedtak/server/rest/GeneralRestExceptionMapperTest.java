package no.nav.vedtak.server.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.UriInfo;

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
import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.util.MemoryAppender;

@Execution(ExecutionMode.SAME_THREAD)
class GeneralRestExceptionMapperTest {

    private static MemoryAppender logSniffer;

    private final GeneralRestExceptionMapper exceptionMapper = new GeneralRestExceptionMapper();

    @BeforeEach
    void setUp() {
        logSniffer = MemoryAppender.sniff(RestServerFeilUtils.class);
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

            assertThat(feilDto.feilkode()).isEqualTo(Feilkode.IKKE_TILGANG.name());
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


    @Test
    void skalLoggeUkjentStiPåInfoMedMaskertSti() {
        var uriInfo = mock(UriInfo.class);
        var request = mock(Request.class);
        when(uriInfo.getMatchedResources()).thenReturn(List.of());
        when(uriInfo.getRequestUri()).thenReturn(URI.create("https://host/fpsak/api/person/12345678901/f.nr"));
        when(request.getMethod()).thenReturn("GET");
        var mapper = new GeneralRestExceptionMapper(uriInfo, request);

        try (var response = mapper.toResponse(new NotFoundException())) {

            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(logSniffer.getLoggedEvents())
                .singleElement()
                .satisfies(e -> {
                    assertThat(e.getLevel()).isEqualTo(Level.INFO);
                    assertThat(e.getFormattedMessage()).isEqualTo("Ukjent sti: GET /fpsak/api/person/***/f.nr");
                    assertThat(e.getThrowableProxy()).isNull();
                });
        }
    }

    @Test
    void skalLoggeNotFoundFraMatchetRessursSomFør() {
        var uriInfo = mock(UriInfo.class);
        when(uriInfo.getMatchedResources()).thenReturn(List.of(new Object()));
        var mapper = new GeneralRestExceptionMapper(uriInfo, mock(Request.class));

        try (var response = mapper.toResponse(new NotFoundException("finnes ikke"))) {

            assertThat(response.getStatus()).isEqualTo(404);
            assertThat(logSniffer.search("Fikk uventet feil: finnes ikke", Level.WARN)).hasSize(1);
        }
    }

    @Test
    void skalSanereSti() {
        assertThat(RestServerFeilUtils.sanerSti("/a\n/12345678901/1234567890123/999999999/v1")).isEqualTo("/a/***/***/***/v1");
        assertThat(RestServerFeilUtils.sanerSti("/" + "a".repeat(500))).hasSizeLessThanOrEqualTo(202);
        assertThat(RestServerFeilUtils.sanerSti(null)).isNull();
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

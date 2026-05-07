package no.nav.vedtak.server.rest.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JacksonException;

import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;
import tools.jackson.core.exc.UnexpectedEndOfInputException;
import tools.jackson.databind.exc.InvalidTypeIdException;

class Jackson3ExceptionMapperTest {

    record A(LocalDateTime tidspunkt) {}

    @Test
    void genererFeilTilfelle1() {
        try {
            DefaultJsonMapper.getJsonMapper().readerFor(FeilDto.class).readValue("{ \"feilkoder\": } \"123\" }");
        } catch (JacksonException e) {
            try (var resultat = new Jackson2ExceptionMapper().toResponse(e)) {
                var dto = (FeilDto) resultat.getEntity();
                assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil: Unexpected character ");
            }
        }
    }

    @Test
    void genererFeilTilfelle2() {
        try {
            DefaultJsonMapper.getJsonMapper().readerFor(A.class).readValue("{ \"tidspunkt\": 123 }");
        } catch (JacksonException e) {
            try (var resultat = new Jackson2ExceptionMapper().toResponse(e)) {
                var dto = (FeilDto) resultat.getEntity();
                assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil: raw timestamp");
            }
        }
    }

    @Test
    void skal_mappe_InvalidTypeIdException() {
        try (var resultat = new Jackson3ExceptionMapper().toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"))) {
            var dto = (FeilDto) resultat.getEntity();
            assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil");
        }
    }

    @Test
    void skal_parse_JsonEOFException(){
        var feilTekst = "Ukjent EOF";
        try (var resultat = new Jackson3ExceptionMapper().toResponse(new UnexpectedEndOfInputException(null, null, feilTekst))) {
            var dto = (FeilDto) resultat.getEntity();

            assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil: " + feilTekst);
        }
    }

}

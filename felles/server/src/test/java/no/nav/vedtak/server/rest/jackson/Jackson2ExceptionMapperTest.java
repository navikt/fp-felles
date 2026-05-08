package no.nav.vedtak.server.rest.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

import no.nav.vedtak.feil.FeilDto;

class Jackson2ExceptionMapperTest {

    @Test
    void skal_mappe_InvalidTypeIdException() {
        try (var resultat = new Jackson2ExceptionMapper().toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"))) {
            var dto = (FeilDto) resultat.getEntity();
            assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil");
        }
    }

    @Test
    void skal_parse_JsonEOFException(){
        var feilTekst = "Ukjent EOF";
        try (var resultat = new Jackson2ExceptionMapper().toResponse(new JsonEOFException(null, null, feilTekst))) {
            var dto = (FeilDto) resultat.getEntity();

            assertThat(dto.feilmelding()).contains("FP-252294 JSON-feil: " + feilTekst);
        }
    }

}

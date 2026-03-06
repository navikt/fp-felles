package no.nav.vedtak.server.rest.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

import no.nav.vedtak.feil.FeilDto;

class Jackson2ExceptionMapperTest {

    @Test
    void skal_mappe_InvalidTypeIdException() {
        try (var resultat = new JsonMappingExceptionMapper().toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"))) {
            var dto = (FeilDto) resultat.getEntity();
            assertThat(dto.feilmelding()).contains("JSON-mapping feil");
            assertThat(dto.safeFeltFeil()).isEmpty();
        }
    }

    @Test
    void skal_parse_JsonEOFException(){
        var feilTekst = "Ukjent EOF";
        try (var resultat = new JsonParseExceptionMapper().toResponse(new JsonEOFException(null, null, feilTekst))) {
            var dto = (FeilDto) resultat.getEntity();

            assertThat(dto.feilmelding()).contains("JSON-parsing feil: " + feilTekst);
            assertThat(dto.feltFeil()).isEmpty();
        }
    }

}

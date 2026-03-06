package no.nav.vedtak.server.rest.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.feil.FeilDto;
import tools.jackson.core.exc.UnexpectedEndOfInputException;
import tools.jackson.databind.exc.InvalidTypeIdException;

class Jackson3ExceptionMapperTest {

    @Test
    void skal_mappe_InvalidTypeIdException() {
        try (var resultat = new DatabindExceptionMapper().toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"))) {
            var dto = (FeilDto) resultat.getEntity();
            assertThat(dto.feilmelding()).contains("JSON-mapping feil");
            assertThat(dto.safeFeltFeil()).isEmpty();
        }
    }

    @Test
    void skal_parse_JsonEOFException(){
        var feilTekst = "Ukjent EOF";
        try (var resultat = new StreamReadExceptionMapper().toResponse(new UnexpectedEndOfInputException(null, null, feilTekst))) {
            var dto = (FeilDto) resultat.getEntity();

            assertThat(dto.feilmelding()).contains("JSON-parsing feil: " + feilTekst);
            assertThat(dto.feltFeil()).isEmpty();
        }
    }

}

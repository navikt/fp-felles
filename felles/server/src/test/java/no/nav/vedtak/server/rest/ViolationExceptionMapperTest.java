package no.nav.vedtak.server.rest;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.FeilType;

class ViolationExceptionMapperTest {

    private record TestRec(@Min(10) int tall, @Pattern(regexp = "\\d{5}") String tekst) { }

    @Test
    void test_validerer_OK() {
        var dto = new TestRec(50, "55555");
        try (var respons = valider(dto)){
            assertThat(respons.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(respons.getEntity()).isNull();
        }
    }

    @Test
    void test_validerer_tallFeil() {
        var dto = new TestRec(5, "55555");
        try (var respons = valider(dto)){
            var feilene = (FeilDto) respons.getEntity();
            assertThat(feilene.feiltype()).isEqualTo(FeilType.VALIDERINGSFEIL.name());
            assertThat(feilene.feilmelding()).contains("TestRec.tall");
            assertThat(feilene.feilmelding()).contains("greater than or equal to 10");
        }
    }

    @Test
    void test_validerer_tekstFeil() {
        var dto = new TestRec(25, "Logg<script>alert(1)</script>Slutt%0a%0d[ERROR]%20Wake%20up");
        try (var respons = valider(dto)){
            assertThat(respons.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
            var feilene = (FeilDto) respons.getEntity();
            assertThat(feilene.feiltype()).isEqualTo(FeilType.VALIDERINGSFEIL.name());
            assertThat(feilene.feilmelding()).contains("TestRec.tekst");
            assertThat(feilene.feilmelding()).contains("d{5}");
        }
    }

    @Test
    void test_validerer_multiFeil() {
        var dto = new TestRec(2, "EnHeltFeilTekst");
        try (var respons = valider(dto)){
            assertThat(respons.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
            var feilene = (FeilDto) respons.getEntity();
            assertThat(feilene.feiltype()).isEqualTo(FeilType.VALIDERINGSFEIL.name());
            assertThat(feilene.feilmelding()).contains("TestRec.tall");
            assertThat(feilene.feilmelding()).contains("TestRec.tekst");
            assertThat(feilene.feilmelding()).contains("d{5}");
            assertThat(feilene.feilmelding()).contains("greater than or equal to 10");
        }
    }

    private Response valider(Object roundTripped) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(roundTripped);
            if (violations.isEmpty()) {
                return Response.ok().build();
            } else {
                var ex = new ConstraintViolationException(violations);
                return new ValidationExceptionMapper().toResponse(ex);
            }
        }
    }
}

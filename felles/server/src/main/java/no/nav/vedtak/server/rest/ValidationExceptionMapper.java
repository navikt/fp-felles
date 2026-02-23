package no.nav.vedtak.server.rest;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.FeilDto;
import no.nav.vedtak.feil.FeilType;
import no.nav.vedtak.feil.FeltFeilDto;

public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionMapper.class);


    @Override
    public Response toResponse(ConstraintViolationException exception) {
        var feltFeil = getFeltFeil(exception);
        var feltListe = feltFeil.stream().map(FeltFeilDto::navn).toList();
        var feilmelding = String.format("Det oppstod en valideringsfeil på felt %s. Vennligst kontroller at verdier er korrekte.", feltListe);
        var feil = new FeilDto(FeilType.VALIDERINGSFEIL, feilmelding, feltFeil);
        var loggtekst = getLoggTekst(feltFeil);
        LOG.warn(loggtekst);
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(feil)
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private static Set<FeltFeilDto> getFeltFeil(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
            .stream()
            .map(ValidationExceptionMapper::getFeltFeil)
            .collect(Collectors.toSet());
    }

    // Hvis du vil ta med constraintViolation.getInvalidValue() - så vask teksten for å unngå logg-injeksjon (se testcase)
    private static FeltFeilDto getFeltFeil(ConstraintViolation<?> constraintViolation) {
        var root = Optional.ofNullable(constraintViolation.getRootBeanClass()).map(Class::getSimpleName).orElse("null");
        var leaf = Optional.ofNullable(constraintViolation.getLeafBean()).map(Object::getClass).map(Class::getSimpleName).orElse("null");
        var field = Optional.ofNullable(constraintViolation.getPropertyPath()).map(Path::toString).orElse("null");
        var start = Objects.equals(root, leaf) ? leaf : root + "." + leaf;
        return new FeltFeilDto(start + "." + field, constraintViolation.getMessage());
    }

    private static String getLoggTekst(Collection<FeltFeilDto> feil) {
        return "VALIDERINGSFEIL: " + feil.stream()
            .map(f -> f.navn() + " - " + f.melding())
            .collect(Collectors.joining(", "));
    }

}

package no.nav.vedtak.server.rest;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import no.nav.vedtak.feil.Feilkode;
import no.nav.vedtak.log.util.LoggerUtils;

public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        RestServerFeilUtils.ensureCallId();
        var feilmelding = getFeilmeldingTekst(exception);
        RestServerFeilUtils.loggWarning(feilmelding);
        if (RestSikkerLoggFeature.erSikkerloggEnabled()) {
            RestServerFeilUtils.sikkerloggWarning(String.format("%s - input %s", feilmelding, getInputs(exception)));
        }
        return RestServerFeilUtils.responseFra(Response.Status.BAD_REQUEST, Feilkode.VALIDERING, feilmelding);
    }

    public static String getFeilmeldingTekst(ConstraintViolationException exception) {
        var feilTekst = getLoggTekst(exception);
        return String.format("Valideringsfeil for felt %s.", feilTekst);
    }

    private static String getLoggTekst(ConstraintViolationException exception) {
        return getFeltFeil(exception).stream()
            .map(f -> f.navn() + ": " + f.melding())
            .collect(Collectors.joining(", "));
    }

    private static Set<FeltFeil> getFeltFeil(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
            .stream()
            .map(ValidationExceptionMapper::getFeltFeil)
            .collect(Collectors.toSet());
    }

    // Hvis du vil ta med constraintViolation.getInvalidValue() - så vask teksten for å unngå logg-injeksjon (se testcase)
    private static FeltFeil getFeltFeil(ConstraintViolation<?> constraintViolation) {
        var root = Optional.ofNullable(constraintViolation.getRootBeanClass())
            .map(Class::getSimpleName)
            .map(rbn -> rbn.replace("$Proxy$_$$_WeldClientProxy", "").concat("."))
            .orElse("");
        var field = Optional.ofNullable(constraintViolation.getPropertyPath()).map(Path::toString).orElse("null");
        return new FeltFeil(root + field, constraintViolation.getMessage());
    }

    private record FeltFeil(String navn, String melding) {}

    public static Set<String> getInputs(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getInvalidValue)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .map(LoggerUtils::removeLineBreaks)
            .collect(Collectors.toSet());
    }

}

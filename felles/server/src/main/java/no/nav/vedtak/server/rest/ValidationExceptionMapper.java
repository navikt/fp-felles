package no.nav.vedtak.server.rest;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.Feilkode;

public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionMapper.class);


    @Override
    public Response toResponse(ConstraintViolationException exception) {
        FeilRespons.ensureCallId();
        var feilmelding = getFeilmeldingTekst(exception);
        LOG.warn(feilmelding);
        return FeilRespons.fra(Response.Status.BAD_REQUEST, Feilkode.VALIDERING, feilmelding);
    }

    protected static String getFeilmeldingTekst(ConstraintViolationException exception) {
        var feltFeil = getFeltFeil(exception);
        var feilTekst = getLoggTekst(feltFeil);
        return String.format("Det oppstod en valideringsfeil for felt %s.", feilTekst);
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

    private static String getLoggTekst(Collection<FeltFeil> feil) {
        return feil.stream()
            .map(f -> f.navn() + ": " + f.melding())
            .collect(Collectors.joining(", "));
    }

    private record FeltFeil(String navn, String melding) {}

}

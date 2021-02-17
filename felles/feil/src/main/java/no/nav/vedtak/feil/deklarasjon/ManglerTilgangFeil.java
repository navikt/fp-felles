package no.nav.vedtak.feil.deklarasjon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.feil.LogLevel;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "3.0.x", forRemoval = true)
public @interface ManglerTilgangFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel() default LogLevel.ERROR;

    Class<? extends ManglerTilgangException> exceptionClass() default ManglerTilgangException.class;
}

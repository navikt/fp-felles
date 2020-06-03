package no.nav.vedtak.feil.deklarasjon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.feil.LogLevel;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IkkeImplementertFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel() default LogLevel.ERROR;

    Class<? extends ManglerTilgangException> exceptionClass() default ManglerTilgangException.class;
}

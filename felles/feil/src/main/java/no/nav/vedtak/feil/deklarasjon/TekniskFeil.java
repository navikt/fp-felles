package no.nav.vedtak.feil.deklarasjon;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TekniskFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel();

    Class<? extends TekniskException> exceptionClass() default TekniskException.class;
}

package no.nav.vedtak.feil.deklarasjon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.LogLevel;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface TekniskFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel();

    Class<? extends TekniskException> exceptionClass() default TekniskException.class;
}

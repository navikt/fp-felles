package no.nav.vedtak.feil.deklarasjon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.LogLevel;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface IntegrasjonFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel();

    Class<? extends IntegrasjonException> exceptionClass() default IntegrasjonException.class;
}

package no.nav.vedtak.feil.deklarasjon;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrasjonFeil {
    String feilkode();

    String feilmelding();

    LogLevel logLevel();

    Class<? extends IntegrasjonException> exceptionClass() default IntegrasjonException.class;
}

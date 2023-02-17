package no.nav.foreldrepenger.konfig;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface KonfigVerdi {

    Annotation TYPE_LITERAL = new KonfigVerdiTypeLiteral();

    /* Nøkkel for å slå opp verdi. */
    @Nonbinding
    String value() default "";

    @Nonbinding
    boolean required() default true;

    @Nonbinding
    String defaultVerdi() default "";

    @Nonbinding
    String beskrivelse() default "";

    @Nonbinding
    Class<? extends Converter<?>> converter() default NoConverter.class;

    interface Converter<V> {
        V tilVerdi(String verdi);
    }

    class NoConverter implements Converter<String> {

        @Override
        public String tilVerdi(String verdi) {
            return verdi;
        }

    }

    class BooleanConverter implements Converter<Boolean> {

        @Override
        public Boolean tilVerdi(String verdi) {
            return verdi == null ? Boolean.FALSE : Boolean.valueOf(verdi);
        }
    }

    class IntegerConverter implements Converter<Integer> {

        @Override
        public Integer tilVerdi(String verdi) {
            return verdi == null ? null : Integer.valueOf(verdi);
        }
    }

    class LongConverter implements Converter<Long> {

        @Override
        public Long tilVerdi(String verdi) {
            return verdi == null ? null : Long.valueOf(verdi);
        }
    }

    class UriConverter implements Converter<URI> {

        @Override
        public URI tilVerdi(String verdi) {
            try {
                return verdi == null ? null : new URI(verdi);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(
                    "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.net.URI: " + verdi, e);
            }
        }
    }

    class UrlConverter implements Converter<URL> {

        @Override
        public URL tilVerdi(String verdi) {
            try {
                return verdi == null ? null : new URL(verdi);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(
                    "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.net.URL: " + verdi, e);
            }
        }
    }

    class PeriodConverter implements Converter<Period> {

        @Override
        public Period tilVerdi(String verdi) {
            try {
                return verdi == null ? null : Period.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                    "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.Period: " + verdi, e);
            }
        }
    }

    class DurationConverter implements Converter<Duration> {

        @Override
        public Duration tilVerdi(String verdi) {
            try {
                return verdi == null ? null : Duration.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                    "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.Duration: " + verdi, e);
            }
        }
    }

    class LocalDateConverter implements Converter<LocalDate> {

        @Override
        public LocalDate tilVerdi(String verdi) {
            try {
                return verdi == null ? null : LocalDate.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                    "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.LocalDate: " + verdi, e);
            }
        }
    }

    class StringDuplicator implements Converter<String> {

        @Override
        public String tilVerdi(String verdi) {
            return verdi + verdi;
        }
    }

    class KonfigVerdiTypeLiteral extends AnnotationLiteral<KonfigVerdi> implements KonfigVerdi {

        @Override
        public String value() {
            return "";
        }

        @Override
        public String defaultVerdi() {
            return "";
        }

        @Override
        public boolean required() {
            return false;
        }

        @Override
        public String beskrivelse() {
            return "";
        }

        @Override
        public Class<? extends Converter<?>> converter() {
            return NoConverter.class;
        }
    }
}

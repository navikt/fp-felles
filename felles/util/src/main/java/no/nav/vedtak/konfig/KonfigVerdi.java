package no.nav.vedtak.konfig;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
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
    Class<? extends KonfigVerdi.Converter<?>> converter() default NoConverter.class;

    public interface Converter<V> {
        V tilVerdi(String verdi);
    }

    static class NoConverter implements KonfigVerdi.Converter<String> {

        @Override
        public String tilVerdi(String verdi) {
            return verdi;
        }

    }

    public static class BooleanConverter implements KonfigVerdi.Converter<Boolean> {

        @Override
        public Boolean tilVerdi(String verdi) {
            return verdi == null ? Boolean.FALSE : Boolean.valueOf(verdi);
        }
    }

    public static class IntegerConverter implements KonfigVerdi.Converter<Integer> {

        @Override
        public Integer tilVerdi(String verdi) {
            return verdi == null ? null : Integer.valueOf(verdi);
        }
    }

    public static class LongConverter implements KonfigVerdi.Converter<Long> {

        @Override
        public Long tilVerdi(String verdi) {
            return verdi == null ? null : Long.valueOf(verdi);
        }
    }

    public static class UriConverter implements KonfigVerdi.Converter<URI> {

        @Override
        public URI tilVerdi(String verdi) {
            try {
                return verdi == null ? null : new URI(verdi);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(
                        "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.net.URI: " + verdi, e); //$NON-NLS-1$
            }
        }
    }

    public static class PeriodConverter implements KonfigVerdi.Converter<Period> {

        @Override
        public Period tilVerdi(String verdi) {
            try {
                return verdi == null ? null : Period.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                        "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.Period: " + verdi, e); //$NON-NLS-1$
            }
        }
    }

    public static class DurationConverter implements KonfigVerdi.Converter<Duration> {

        @Override
        public Duration tilVerdi(String verdi) {
            try {
                return verdi == null ? null : Duration.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                        "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.Duration: " + verdi, e); //$NON-NLS-1$
            }
        }
    }

    public static class LocalDateConverter implements KonfigVerdi.Converter<LocalDate> {

        @Override
        public LocalDate tilVerdi(String verdi) {
            try {
                return verdi == null ? null : LocalDate.parse(verdi);
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                        "Ugyldig konfigurasjonsparameter, kan ikke konvertere til java.time.LocalDate: " + verdi, e); //$NON-NLS-1$
            }
        }
    }

    static class StringDuplicator implements Converter<String> {

        @Override
        public String tilVerdi(String verdi) {
            return verdi + verdi;
        }
    }

    static class KonfigVerdiTypeLiteral extends AnnotationLiteral<KonfigVerdi> implements KonfigVerdi {

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

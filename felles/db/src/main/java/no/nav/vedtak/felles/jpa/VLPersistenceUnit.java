package no.nav.vedtak.felles.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;
import javax.persistence.PersistenceUnit;

/**
 * Definerer hvilken {@link PersistenceUnit} som skal benyttes.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Inherited
@Documented
public @interface VLPersistenceUnit {

    String value() default VLPersistenceUnitLiteral.DEFAULT_KODE;

}

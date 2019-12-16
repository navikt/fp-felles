package no.nav.vedtak.felles.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.PersistenceUnit;

/**
 * Definerer hvilken {@link PersistenceUnit} som skal benyttes.
 * @deprecated fjern der den brukes, benytter kun en EntityManager default per applikasjon uansett
 */
@Deprecated(forRemoval = true)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
@Inherited
@Documented
public @interface VLPersistenceUnit {

}

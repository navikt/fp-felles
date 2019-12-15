package no.nav.vedtak.felles.jpa;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;
import javax.transaction.Transactional;

/**
 * Annotasjon for å flagge at her trengs en annotasjon.
 * @deprecated Bytt til {@link Transactional} for bedre portabilitet over plattformer. 
 */
@Deprecated(forRemoval = true)
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transaction {

}

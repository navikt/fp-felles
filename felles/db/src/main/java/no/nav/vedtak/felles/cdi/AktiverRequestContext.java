package no.nav.vedtak.felles.cdi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InterceptorBinding;

import org.jboss.weld.context.RequestContext;

/**
 * Annotasjon for å aktivere {@link RequestContext} - et CDI scope som er mappet til pågående request gjennom en
 * ThreadLocal pattern.
 * 
 * {@link RequestContext} er Weld CDI container sin implementasjon av context når {@link RequestScoped} scope er i bruk.
 * 
 * NB: I CDI 2.0 vil dette standardiseres og det vil antagelig ikke bli bruk for denne klassen.
 *
 * @see RequestScoped
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface AktiverRequestContext {

    public static class Literal extends AnnotationLiteral<AktiverRequestContext> implements AktiverRequestContext {
        public static final Literal INSTANCE = new Literal();
    }
}

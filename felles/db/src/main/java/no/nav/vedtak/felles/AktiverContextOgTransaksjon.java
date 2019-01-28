package no.nav.vedtak.felles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.AnnotationLiteral;

import no.nav.vedtak.felles.cdi.AktiverRequestContext;
import no.nav.vedtak.felles.jpa.Transaction;

/**
 * En CDI {@link Stereotype} som aktiverer en sikkerhet og transaksjonsgrense samtidig for å forenkle det vanlige caser
 * der begge må settes opp samtidig.
 * 
 * Setter opp CDI RequestContext scope hvis nødvendig
 *
 * @see RequestScoped
 * @see Transaction
 * 
 */
@Transaction
@AktiverRequestContext
@Stereotype
@Inherited
// NB: Stereotype + InterceptorBinding annotations fungerer bare dersom @Target er deklarert som KUN ElementType.TYPE i CDI 1.2
// ref: https://docs.jboss.org/cdi/spec/1.2/cdi-spec.html#stereotype_interceptor_bindings
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AktiverContextOgTransaksjon {

    public static class Literal extends AnnotationLiteral<AktiverContextOgTransaksjon> implements AktiverContextOgTransaksjon {
        public static final Literal INSTANCE = new Literal();
    }
}

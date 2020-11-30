package no.nav.vedtak.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface BeskyttetRessurs {
    @Nonbinding
    BeskyttetRessursActionAttributt action();

    @Nonbinding
    String resource() default "";

    /**
     * Property hvor resource kan slås opp fra. Først og fremst for biblioteker der
     * resource er forskjellig mellom applikasjoner. Property angis som java
     * property (eks: "abac.rolle"). Dersom ikke tilgjengelig som property tolkes
     * det som Env variabel på upper case (eks. "ABAC_ROLLE").
     */
    @Nonbinding
    String property() default "";

    /**
     * Sett til false for å unngå at det logges til sporingslogg ved tilgang. Det
     * skal bare gjøres for tilfeller som ikke håndterer personopplysninger.
     */
    @Nonbinding
    boolean sporingslogg() default true;
}

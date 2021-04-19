package no.nav.foreldrepenger.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import no.nav.foreldrepenger.sikkerhet.abac.domene.ActionType;
import no.nav.foreldrepenger.sikkerhet.abac.domene.ServiceType;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface BeskyttetRessurs {

    /**
     * Property som avgjør om bl. annet hva slags token skal brukes.
     * Def fleste tjenester implementerer REST så det er default men WS will trenge SAML.
     */
    @Nonbinding
    ServiceType service() default ServiceType.REST;

    /**
     * Property som beskriver CRUD aksjon utført av tjenesten.
     */
    @Nonbinding
    ActionType action();

    /**
     * Ressurs type knyttet til ABAC policy man beskyttet tilgang til.
     * Må ikke settes om property() brukes.
     */
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

    /**
     * Path til tjenseten uten base_url. Brukes til sporingslogge tilgang til en konkrett tjeneste.
     * Bør starte med / og være representert av @Path for alle REST tjenester eller med @Method for WS.
     */
    @Nonbinding
    String path();
}

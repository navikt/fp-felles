package no.nav.vedtak.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import jakarta.ws.rs.NameBinding;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@NameBinding
public @interface BeskyttetRessurs {

    /**
     * Handling
     */
    @Nonbinding ActionType actionType() default ActionType.DUMMY;

    /**
     * Handling skal utføres på ressurs
     */
    @Nonbinding ResourceType resourceType() default ResourceType.DUMMY;

    /**
     * For å angi om tjeneste skal kunne kalles fra andre namespace
     */
    @Nonbinding AvailabilityType availabilityType() default AvailabilityType.INTERNAL;

    /**
     * Sett til false for å unngå at det logges til sporingslogg ved tilgang. Det
     * skal bare gjøres for tilfeller som ikke håndterer personopplysninger.
     */
    @Nonbinding boolean sporingslogg() default true;
}

package no.nav.vedtak.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import javax.ws.rs.NameBinding;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.AvailabilityType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;

@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@NameBinding
public @interface BeskyttetRessurs {
    @Nonbinding
    ActionType actionType() default ActionType.DUMMY;

    /**
     * Tre alternativ for å spesifisere resource
     * - String resource - brukes typisk for andre domener enn de som er tilgjengelig under ResourceType
     * - ResourceType - velg fra en enum - NB kun foreldrepenger, ikke duplo eller k9
     * - String property - for å hente verdi fra en property, se under
     */
    @Nonbinding
    String resource() default "";

    @Nonbinding
    ResourceType resourceType() default ResourceType.DUMMY;

    /**
     * Property hvor resource kan slås opp fra.
     * Først og fremst for biblioteker der resource er forskjellig mellom applikasjoner.
     * Property angis som java property (eks: "abac.rolle").
     * Dersom ikke tilgjengelig som property tolkes det som Env variabel på upper case (eks. "ABAC_ROLLE").
     */
    @Nonbinding
    String property() default "";

    /** For å angi webservices (noen få tilfelle) */
    @Nonbinding
    ServiceType serviceType() default ServiceType.REST;

    /** For å angi om tjeneste skal kunne kalles fra andre namespace */
    @Nonbinding
    AvailabilityType availabilityType() default AvailabilityType.INTERNAL;

    /**
     * Sett til false for å unngå at det logges til sporingslogg ved tilgang. Det
     * skal bare gjøres for tilfeller som ikke håndterer personopplysninger.
     */
    @Nonbinding
    boolean sporingslogg() default true;
}

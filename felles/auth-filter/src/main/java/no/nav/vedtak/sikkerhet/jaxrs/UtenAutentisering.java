package no.nav.vedtak.sikkerhet.jaxrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/*
 * Primært for endepunkt som kalles av plattformen (liveness, prestop, ...)
 * Kan vurdere å legge til TYPE ved behov for å annotere hele interface eller klasser
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@NameBinding
public @interface UtenAutentisering {
}

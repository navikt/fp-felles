package no.nav.foreldrepenger.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

import no.nav.foreldrepenger.sikkerhet.abac.domene.AbacDataAttributter;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface AbacDtoSupplier {

    Class<? extends Function<Object, AbacDataAttributter>> supplierClass();

}

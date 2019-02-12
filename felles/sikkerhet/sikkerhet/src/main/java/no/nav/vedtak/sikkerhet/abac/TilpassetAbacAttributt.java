package no.nav.vedtak.sikkerhet.abac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface TilpassetAbacAttributt {

    Class<? extends Function<Object, AbacDataAttributter>> supplierClass();

}

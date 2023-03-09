package no.nav.vedtak.sikkerhet.abac;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@NameBinding
public @interface ÅpenRessurs {
}

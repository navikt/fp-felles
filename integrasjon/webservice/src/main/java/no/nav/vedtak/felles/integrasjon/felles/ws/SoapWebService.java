package no.nav.vedtak.felles.integrasjon.felles.ws;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface SoapWebService {

    String endpoint();

    String tjenesteBeskrivelseURL();

}

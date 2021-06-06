package no.nav.vedtak.felles.integrasjon.rest.jersey;

import javax.ws.rs.client.Invocation;

public interface Invoker {

    <T> T invoke(Invocation i, Class<T> clazz);

    <T> T invoke(Invocation i, Class<T> clazz, Class<? extends Exception>[] translatableExceptions);

}

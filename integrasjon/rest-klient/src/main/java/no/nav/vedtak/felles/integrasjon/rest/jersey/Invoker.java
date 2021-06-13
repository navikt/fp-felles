package no.nav.vedtak.felles.integrasjon.rest.jersey;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

@SuppressWarnings("unchecked")
public interface Invoker {

    <T> T invoke(Invocation i, Class<T> clazz);

    <T> T invoke(Invocation i, Class<T> clazz, Class<? extends RuntimeException>... translatableExceptions);

    void invoke(Invocation i, Class<? extends RuntimeException>... translatableExceptions);

    <T> T invoke(Invocation i, GenericType<T> type, Class<? extends RuntimeException>... translatableExceptions);

    void invoke(Invocation i);

}

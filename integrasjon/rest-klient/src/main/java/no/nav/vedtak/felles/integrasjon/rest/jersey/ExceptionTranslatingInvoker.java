package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.util.Arrays;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import no.nav.vedtak.exception.IntegrasjonException;

@SuppressWarnings("unchecked")
public class ExceptionTranslatingInvoker implements Invoker {

    @Override
    public void invoke(Invocation i) {
        invoke(i, ProcessingException.class);
    }

    @Override
    public <T> T invoke(Invocation i, Class<T> clazz) {
        return invoke(i, clazz, ProcessingException.class);
    }

    @Override
    public <T> T invoke(Invocation i, Class<T> clazz, Class<? extends RuntimeException>... translatables) {
        try {
            return i.invoke(clazz);
        } catch (RuntimeException e) {
            return handle(e, translatables);
        }
    }

    @Override
    public void invoke(Invocation i, Class<? extends RuntimeException>... translatables) {
        try {
            i.invoke();
        } catch (RuntimeException e) {
            handle(e, translatables);
        }
    }

    @Override
    public <T> T invoke(Invocation i, GenericType<T> type, Class<? extends RuntimeException>... translatables) {
        try {
            return i.invoke(type);
        } catch (RuntimeException e) {
            return handle(e, translatables);
        }
    }

    private static <T> T handle(RuntimeException e, Class<? extends RuntimeException>... translatables) {
        throw Arrays.stream(translatables)
                .filter(t -> t.isAssignableFrom(e.getClass()))
                .findFirst()
                .map(t -> translate(e))
                .orElseGet(() -> e);
    }

    private static RuntimeException translate(RuntimeException e) {
        throw new IntegrasjonException("F-999999", "Oversatte exception " + e.getClass().getName(), e);
    }
}

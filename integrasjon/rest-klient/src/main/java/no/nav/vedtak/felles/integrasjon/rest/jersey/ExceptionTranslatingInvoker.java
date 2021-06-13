package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;

import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLException;

@SuppressWarnings("unchecked")
public class ExceptionTranslatingInvoker implements Invoker {

    private final Class<? extends VLException> translatedException;

    public ExceptionTranslatingInvoker() {
        this(IntegrasjonException.class);
    }

    public ExceptionTranslatingInvoker(Class<? extends VLException> translatedException) {
        this.translatedException = translatedException;
    }

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

    private <T> T handle(RuntimeException e, Class<? extends RuntimeException>... translatables) {
        throw Stream.concat(Set.of(translatables).stream(),
                Stream.of(ProcessingException.class))
                .collect(toSet())
                .stream()
                .filter(t -> t.isAssignableFrom(e.getClass()))
                .findFirst()
                .map(t -> this.translate(e))
                .orElseGet(() -> e);
    }

    private RuntimeException translate(RuntimeException e) {
        try {
            throw invokeConstructor(translatedException, "F-999999", "Oversatte exception " + e.getClass().getName(), e);
        } catch (VLException v) {
            throw v;
        } catch (Exception e1) {
            throw new IllegalArgumentException(e1);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [translatedException=" + translatedException + "]";
    }
}

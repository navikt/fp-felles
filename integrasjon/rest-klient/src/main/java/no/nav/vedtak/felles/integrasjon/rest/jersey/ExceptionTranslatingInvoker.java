package no.nav.vedtak.felles.integrasjon.rest.jersey;

import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;

import no.nav.vedtak.exception.IntegrasjonException;

public class ExceptionTranslatingInvoker {

    private List<Class<? extends Exception>> translatableExceptions;

    public ExceptionTranslatingInvoker() {
        this(List.of(ProcessingException.class));
    }

    public ExceptionTranslatingInvoker(List<Class<? extends Exception>> translatableExceptions) {
        this.translatableExceptions = translatableExceptions;
    }

    public <T> T invoke(Invocation i, Class<T> clazz) {
        try {
            return i.invoke(clazz);
        } catch (Exception ex) {
            for (var te : translatableExceptions) {
                if (te.isAssignableFrom(ex.getClass())) {
                    throw new IntegrasjonException("F-999999", "Oversatte exception " + ex.getClass().getName(), ex);
                }
            }
            throw ex;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [translatableExceptions=" + translatableExceptions + "]";
    }
}

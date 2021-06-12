package no.nav.vedtak.felles.integrasjon.rest.jersey;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;

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
    public <T> T invoke(Invocation i, Class<T> clazz, Class<? extends Exception>... translatableExceptions) {
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
    public void invoke(Invocation i, Class<? extends Exception>... translatableExceptions) {
        try {
            i.invoke();
        } catch (Exception ex) {
            for (var te : translatableExceptions) {
                if (te.isAssignableFrom(ex.getClass())) {
                    throw new IntegrasjonException("F-999999", "Oversatte exception " + ex.getClass().getName(), ex);
                }
            }
            throw ex;
        }
    }

}
